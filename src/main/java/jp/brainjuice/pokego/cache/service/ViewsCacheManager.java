package jp.brainjuice.pokego.cache.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jp.brainjuice.pokego.business.service.search.utils.PokemonEditUtils;
import jp.brainjuice.pokego.cache.inmemory.topic.data.PageNameEnum;
import jp.brainjuice.pokego.dao.jpa.PageViewRepository;
import jp.brainjuice.pokego.dao.jpa.PokemonViewRepository;
import jp.brainjuice.pokego.dao.jpa.RaceDiffSearchHistoryRepository;
import jp.brainjuice.pokego.dao.jpa.entity.PageView;
import jp.brainjuice.pokego.dao.jpa.entity.PokemonView;
import jp.brainjuice.pokego.dao.jpa.entity.RaceDiffSearchHistory;
import jp.brainjuice.pokego.utils.BjUtils;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;

/**
 * 閲覧情報をDBへ同期するためのクラスです。<br>
 * アクセス時にRedisに一時保存(インクリメント)された閲覧情報を、
 * 日次のバッチ処理でPostgreSQLへ同期します。
 *
 * @author saibabanagchampa
 */
@Component
@Slf4j
public class ViewsCacheManager {

	private PageViewRepository pageViewRepository;
	private PokemonViewRepository pokemonViewRepository;
	private RaceDiffSearchHistoryRepository raceDiffSearchHistoryRepository;
	private StringRedisTemplate redisTemplate;

	// 一時保存用RedisHashキー
	private static final String HASH_PAGE = "pending_db_views:page";
	private static final String HASH_POKEMON = "pending_db_views:pokemon";
	private static final String HASH_RACE_DIFF = "pending_db_views:raceDiff";

	// 話題の検索の集計対象外ページ
	private static final List<PageNameEnum> IGNORE_TOPIC_PAGE_LIST = List.of(PageNameEnum.abundance, PageNameEnum.home);

	public ViewsCacheManager(
			PageViewRepository pageViewRepository,
			PokemonViewRepository pokemonViewRepository,
			RaceDiffSearchHistoryRepository raceDiffSearchHistoryRepository,
			StringRedisTemplate redisTemplate) {
		this.pageViewRepository = pageViewRepository;
		this.pokemonViewRepository = pokemonViewRepository;
		this.raceDiffSearchHistoryRepository = raceDiffSearchHistoryRepository;
		this.redisTemplate = redisTemplate;
	}

	/**
	 * 日次バッチ処理
	 * 毎日0時にRedis上の保留中閲覧数をDBに一括登録する。
	 * ShedLockにより複数APサーバ起動時も1台のみ実行される。
	 */
	@Scheduled(cron = "0 0 0 * * ?") // 毎日0時に実行
	@SchedulerLock(name = "daily_view_sync_lock", lockAtMostFor = "PT10M")
	public void syncDailyViewsToDb() {
		log.info("> Start daily view sync from Redis to PostgreSQL");

		// 1. 各ハッシュの全エントリを取得
		HashOperations<String, String, String> hashOps = redisTemplate.opsForHash();
		Map<String, String> pageViews = hashOps.entries(HASH_PAGE);
		Map<String, String> pokemonViews = hashOps.entries(HASH_POKEMON);
		Map<String, String> raceDiffViews = hashOps.entries(HASH_RACE_DIFF);

		// 2. Redis上のハッシュを削除（同期対象をクリア）
		// ※実行中に新しいアクセスが来た場合、この瞬間削除された後に新規作成されるため安全
		redisTemplate.delete(List.of(HASH_PAGE, HASH_POKEMON, HASH_RACE_DIFF));

		// 3. PostgreSQLへ保存（昨日分のデータとして保存する）
		Date targetDate = BjUtils.toDate(BjUtils.nowLocalDate().minusDays(1));

		if (!pageViews.isEmpty()) {
			List<PageView> pageViewList = syncPageViews(pageViews, targetDate);
			pageViewRepository.saveAll(pageViewList);
		}
		if (!pokemonViews.isEmpty()) {
			List<PokemonView> pokemonViewList = syncPokemonViews(pokemonViews, targetDate);
			pokemonViewRepository.saveAll(pokemonViewList);
		}
		if (!raceDiffViews.isEmpty()) {
			List<RaceDiffSearchHistory> raceDiffSearchHistoryList = syncRaceDiffViews(raceDiffViews);
			raceDiffSearchHistoryRepository.saveAll(raceDiffSearchHistoryList);
		}

		log.info(MessageFormat.format("> End daily view sync.(Redis -> PostgreSQL) page:{0}, pokemon:{1}, raceDiff:{2}",
				pageViews.size(), pokemonViews.size(), raceDiffViews.size()));
	}

	/**
	 * Redisに蓄積されたページごとの保留中の閲覧数を、指定した日付のデータとしてPostgreSQLへ同期します。
	 * 既にレコードが存在する場合はカウントを加算し、存在しない場合は新規作成します。
	 *
	 * @param pageViewMap Redisから取得したページ名と閲覧数のマップ
	 * @param targetDate  保存対象となる日付（通常は前日）
	 * @return 保存したPageViewエンティティのリスト
	 */
	private List<PageView> syncPageViews(Map<String, String> pageViewMap, Date targetDate) {
		List<PageView> pageViewList = pageViewRepository.findAllByYmd(targetDate);
		List<PageView> saveList = new ArrayList<>();

		for (Map.Entry<String, String> entry : pageViewMap.entrySet()) {
			String page = entry.getKey();
			int viewCount = Integer.parseInt(entry.getValue());

			Optional<PageView> pvOpt = pageViewList.stream()
					.filter(pvRecords -> page.equals(pvRecords.getPage())).findAny();

			PageView pv;
			if (pvOpt.isPresent()) {
				pv = pvOpt.get();
				pv.setViewCount(pv.getViewCount() + viewCount);
			} else {
				pv = new PageView();
				pv.setPage(page);
				pv.setYmd(targetDate);
				pv.setViewCount(viewCount);
			}
			saveList.add(pv);
		}
		return saveList;
	}

	/**
	 * Redisに蓄積されたポケモンごとの保留中の閲覧数を、指定した日付のデータとしてPostgreSQLへ同期します。
	 * 既にレコードが存在する場合はカウントを加算し、存在しない場合は新規作成します。
	 *
	 * @param pokemonViewMap Redisから取得したポケモンIDと閲覧数のマップ
	 * @param targetDate     保存対象となる日付（通常は前日）
	 * @return 保存したPokemonViewエンティティのリスト
	 */
	private List<PokemonView> syncPokemonViews(Map<String, String> pokemonViewMap, Date targetDate) {
		List<PokemonView> pokemonViewList = pokemonViewRepository.findAllByYmd(targetDate);
		List<PokemonView> saveList = new ArrayList<>();

		for (Map.Entry<String, String> entry : pokemonViewMap.entrySet()) {
			String pid = entry.getKey();
			int viewCount = Integer.parseInt(entry.getValue());

			Optional<PokemonView> pvOpt = pokemonViewList.stream()
					.filter(pvRecords -> pid.equals(pvRecords.getPokedexId())).findAny();

			PokemonView pv;
			if (pvOpt.isPresent()) {
				pv = pvOpt.get();
				pv.setViewCount(pv.getViewCount() + viewCount);
			} else {
				pv = new PokemonView();
				pv.setPokedexId(pid);
				pv.setYmd(targetDate);
				pv.setViewCount(viewCount);
			}
			saveList.add(pv);
		}
		return saveList;
	}

	/**
	 * Redisに蓄積された種族値比較の検索履歴（複数のポケモンIDの組み合わせ）の回数をPostgreSQLへ同期します。
	 * IDの組み合わせからSHA-256ハッシュを生成して一意のキーとし、既存レコードがあれば加算、なければ新規作成します。
	 *
	 * @param raceDiffMap Redisから取得した比較対象IDのカンマ区切り文字列と検索回数のマップ
	 */
	private List<RaceDiffSearchHistory> syncRaceDiffViews(Map<String, String> raceDiffMap) {
		List<RaceDiffSearchHistory> saveList = new ArrayList<>();
		LocalDateTime nowDt = LocalDateTime.now();

		for (Map.Entry<String, String> entry : raceDiffMap.entrySet()) {
			String joinedIds = entry.getKey();
			int viewCount = Integer.parseInt(entry.getValue());
			String[] ids = joinedIds.split(",");

			try {
				MessageDigest digest = MessageDigest.getInstance("SHA-256");
				byte[] hashBytes = digest.digest(joinedIds.getBytes(StandardCharsets.UTF_8));
				StringBuilder sb = new StringBuilder();
				for (byte b : hashBytes) {
					sb.append(String.format("%02x", b));
				}
				String searchHash = sb.toString();

				Optional<RaceDiffSearchHistory> opt = raceDiffSearchHistoryRepository.findById(searchHash);
				if (opt.isPresent()) {
					RaceDiffSearchHistory history = opt.get();
					history.setSearchCount(history.getSearchCount() + viewCount);
					history.setLastSearchedAt(nowDt);
					saveList.add(history);
				} else {
					RaceDiffSearchHistory history = new RaceDiffSearchHistory();
					if (ids.length > 0)
						history.setPokedexId1(ids[0]);
					if (ids.length > 1)
						history.setPokedexId2(ids[1]);
					if (ids.length > 2)
						history.setPokedexId3(ids[2]);
					if (ids.length > 3)
						history.setPokedexId4(ids[3]);
					if (ids.length > 4)
						history.setPokedexId5(ids[4]);
					if (ids.length > 5)
						history.setPokedexId6(ids[5]);
					history.setSearchCount(viewCount);
					history.setLastSearchedAt(nowDt);
					history.setSearchHash(searchHash);
					saveList.add(history);
				}
			} catch (NoSuchAlgorithmException e) {
				log.error("SHA-256 algorithm not found", e);
			}
		}
		return saveList;
	}

	/**
	 * 閲覧情報をRedisに追加する。（画面表示タイミングでの閲覧数加算処理）
	 * 連打防止(SETNX)を行い、HashとZSETにカウントアップを記録する。
	 *
	 * @param page
	 * @param pokedexIds
	 * @param ip
	 */
	public void addView(PageNameEnum page, List<String> pokedexIds, String ip) {
		String targetStr = "";
		if (pokedexIds != null && !pokedexIds.isEmpty()) {
			if (pokedexIds.size() > 1) {
				// pokedexIdが複数ある場合は、ソートして連結する
				List<String> sortedIds = new ArrayList<>(pokedexIds);
				sortedIds.sort(PokemonEditUtils.getPokedexIdComparator());
				targetStr = String.join(",", sortedIds);
			} else {
				targetStr = String.join(",", pokedexIds);
			}
		}

		String lockKey = String.format("view_lock:%s:%s:%s", ip, page.name(), targetStr);
		boolean acquired = redisTemplate.opsForValue()
				.setIfAbsent(lockKey, "1", 5, java.util.concurrent.TimeUnit.MINUTES).booleanValue();

		if (!acquired) {
			// 5分以内に同一条件での画面表示をしていた場合、連打抑止としてアクセスを無視する。
			return;
		}

		java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHH");
		String currentHour = LocalDateTime.now().format(formatter);

		// ページ閲覧数のカウントアップ (Redis Hash/ZSET)
		if (!page.getJpn().isEmpty()) {
			String pageName = page.name();
			redisTemplate.opsForHash().increment("pending_db_views:page", pageName, 1);

			// 話題のページのZSET
			if (!IGNORE_TOPIC_PAGE_LIST.contains(page)) {
				String zsetKey = "trending:page:" + currentHour;
				redisTemplate.opsForZSet().incrementScore(zsetKey, pageName, 1);
				redisTemplate.expire(zsetKey, 72, java.util.concurrent.TimeUnit.HOURS);
			}
		}

		if (pokedexIds != null && !pokedexIds.isEmpty()) {
			// ポケモンごとの閲覧数
			for (String id : pokedexIds) {
				// ポケモン閲覧数のカウントアップ
				redisTemplate.opsForHash().increment("pending_db_views:pokemon", id, 1);

				// 話題のポケモンのためのZSET
				String zsetKey = "trending:pokemon:" + currentHour;
				redisTemplate.opsForZSet().incrementScore(zsetKey, id, 1);
				redisTemplate.expire(zsetKey, 72, java.util.concurrent.TimeUnit.HOURS);
			}

			if (page == PageNameEnum.raceDiff) {
				// DB同期用保留ハッシュ (RaceDiff)
				redisTemplate.opsForHash().increment("pending_db_views:raceDiff", targetStr, 1);
			}
		}
	}
}
