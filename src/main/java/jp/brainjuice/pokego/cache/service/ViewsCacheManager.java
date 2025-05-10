package jp.brainjuice.pokego.cache.service;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jp.brainjuice.pokego.business.service.search.utils.PokemonEditUtils;
import jp.brainjuice.pokego.cache.inmemory.topic.ViewTempInfo;
import jp.brainjuice.pokego.cache.inmemory.topic.ViewTempList;
import jp.brainjuice.pokego.dao.jpa.PageViewRepository;
import jp.brainjuice.pokego.dao.jpa.PokemonViewRepository;
import jp.brainjuice.pokego.dao.jpa.entity.PageView;
import jp.brainjuice.pokego.dao.jpa.entity.PokemonView;
import jp.brainjuice.pokego.dao.redis.PageTempViewRedisRepository;
import jp.brainjuice.pokego.dao.redis.PokemonTempViewRedisRepository;
import jp.brainjuice.pokego.dao.redis.entity.PageTempView;
import jp.brainjuice.pokego.dao.redis.entity.PokemonTempView;
import jp.brainjuice.pokego.utils.BjUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * 閲覧情報を管理するためのクラスです。<br>
 * Redisに一時敵に保存した閲覧情報は、最終的にPostgreSQL上に保存します。<br>
 * ＜流れ＞<br>
 * 1.ページを閲覧する→メモリに保存(ViewTempList)
 * 2.15分ごとにRedisとPostgresに送信する。
 * 3.15分ごとに、15分以上経過した閲覧情報がないか確認し、存在した場合削除する。
 *
 * TopicListでは、2.の手順で追加したRedisサーバの情報を参照する。<br>
 * （Redisでは存続期間を設定していて、一定時間経った閲覧情報から順に削除されていく。）
 *
 * @author saibabanagchampa
 *
 */
@Component
@Slf4j
public class ViewsCacheManager {

	private ViewTempList viewTempList;

	private PageViewRepository pageViewRepository;

	private PokemonViewRepository pokemonViewRepository;

	/** Redis上の一時的なページ閲覧情報を管理するためのリポジトリ */
	private PageTempViewRedisRepository pageTempViewRedisRepository;
	/** Redis上の一時的なポケモン閲覧情報を管理するためのリポジトリ */
	private PokemonTempViewRedisRepository pokemonTempViewRedisRepository;
	
	private SetOperations<String, String> setOperations;

	private static final String START_MSG_SCHEDULE = "Start ViewInfo(page, pokemon) schedule.";
	private static final String END_MSG_SCHEDULE = "End ViewInfo(page. pokemon) schedule.";

	private static final String START_MSG_INCR_VIEWS_COUNT_INFO = "> Start incr ViewsCount.(memory -> PostgreSQL)";
	private static final String END_MSG_INCR_VIEWS_COUNT_INFO = "> End incr ViewsCount. page:{0}, pokemon:{1}";

	private static final String START_MSG_SEND_VIEW_TEMP_INFO = "> Start send ViewTempInfo.(memory -> Redis)";
	private static final String END_MSG_SEND_VIEW_TEMP_INFO = "> End send ViewTempInfo. page:{0}, pokemon:{1}";

	private static final String DELETE_ALL_TEMP_PAGE_INFO = "Delete All PageTempView.(Redis)";
	private static final String DELETE_ALL_TEMP_POKEMON_INFO = "Delete All PokemonTempView.(Redis)";
	
	private static final String CLEANUP_INFO = "CLEANUP {0}.(count = {1})";
	private static final String CLEANUP_NOTHING_INFO = "CLEANUP {0}. There is nothing to delete.";

	public ViewsCacheManager(
			ViewTempList viewTempList,
			PageViewRepository pageViewRepository,
			PokemonViewRepository pokemonViewRepository,
			PageTempViewRedisRepository pageTempViewRedisRepository,
			PokemonTempViewRedisRepository pokemonTempViewRedisRepository,
			RedisTemplate<String, String> redisTemplate) {
		this.viewTempList = viewTempList;
		this.pokemonViewRepository = pokemonViewRepository;
		this.pageViewRepository = pageViewRepository;
		this.pageTempViewRedisRepository = pageTempViewRedisRepository;
		this.pokemonTempViewRedisRepository = pokemonTempViewRedisRepository;
		this.setOperations = redisTemplate.opsForSet();
	}

	/**
	 * DI上のViewsTempListを取得します。
	 *
	 * @return
	 */
	ViewTempList getViewsTempList() {
		return viewTempList;
	}

	/**
	 * 閲覧情報の送信（キャッシュサーバへ）
	 *
	 * 15分おきに実行
	 * タスク実行完了の15分後
	 * （サーバ起動5分後から開始）
	 */
	@Scheduled(initialDelay = 300000, fixedDelay = 900000)
	public void sendViewInfo() {

		log.info(START_MSG_SCHEDULE);

		// 集計対象の閲覧情報の取得
		List<ViewTempInfo> aggregateTargetList = viewTempList.getAggregateTargetList();

		// キャッシュサーバへの閲覧情報の一時保存
		sendViewsTempInfo(aggregateTargetList);

		// 閲覧数の加算
		incrViewsCount(aggregateTargetList);

		log.info(END_MSG_SCHEDULE);

	}

	/**
	 * PostgreSQL上のページ、ポケモンごとの閲覧数を加算する。
	 *
	 * ページのRedis上のキー名：pageViews
	 * ポケモンのRedis上のキー名：pokemonViews
	 *
	 * @param aggregateTargetList
	 */
	private void incrViewsCount(List<ViewTempInfo> aggregateTargetList) {

		log.debug(START_MSG_INCR_VIEWS_COUNT_INFO);

		// page閲覧情報リスト、pokemon閲覧情報リストに分割する。
		Map<String, Set<ViewTempInfo>> pageViewMap = new HashMap<>();
		Map<String, Set<ViewTempInfo>> pokemonViewMap = new HashMap<>();

		// Mapのvalueに持つSetに閲覧情報を追加する関数。カリー化（引数 => (Map<String, Set<ViewTempInfo>>, String, ViewTempInfo)）
		Function<Map<String, Set<ViewTempInfo>>, Function<String, Consumer<ViewTempInfo>>> addSetFunc = (map) -> (key) -> (value) -> {
			if (key != null) {
				if (map.containsKey(key)) {
					map.get(key).add(value);
				} else {
					Set<ViewTempInfo> viewSet = new HashSet<>();
					viewSet.add(value);
					map.put(key, viewSet);
				}
			}
		};

		// 集計対象の閲覧情報をMapに設定する。
		aggregateTargetList.forEach(vti -> {
			// ページの日本語名が空文字の場合はページ名はカウントしない。
			if (!vti.getPage().getJpn().isEmpty()) {
				// ページの閲覧情報をMapに追加する。
				addSetFunc.apply(pageViewMap).apply(PokemonEditUtils.getStrName(vti.getPage())).accept(vti);
			}
			// ポケモンの閲覧情報をMapに追加する。
			addSetFunc.apply(pokemonViewMap).apply(vti.getPokedexId()).accept(vti);

		});


		/** 閲覧数を加算する */
		// 今日の閲覧数を取得する。
		Date today = BjUtils.toDate(BjUtils.nowLocalDate());
		List<PageView> pageViewList = pageViewRepository.findAllByYmd(today);
		List<PokemonView> pokemonViewList = pokemonViewRepository.findAllByYmd(today);

		// 加算する。
		pageViewRepository.saveAll(createUpdatePageRecords(pageViewMap, pageViewList, today));
		pokemonViewRepository.saveAll(createUpdatePokemonRecords(pokemonViewMap, pokemonViewList, today));

		log.debug(MessageFormat.format(END_MSG_INCR_VIEWS_COUNT_INFO, pageViewMap, pokemonViewMap));

	}

	/**
	 * ページ、ポケモンの閲覧情報をキャッシュサーバ(Redisサーバ)に送信する。<br>
	 * これは一時的に保存する情報であり、存続期間が過ぎると古いものから削除されていく。
	 *
	 * ページのRedis上のキー名：pageTempView
	 * ポケモンのRedis上のキー名：pokemonTempView
	 *
	 * @param aggregateTargetList
	 * @see PageTempView
	 * @see PokemonTempView
	 */
	private void sendViewsTempInfo(List<ViewTempInfo> aggregateTargetList) {

		log.info(START_MSG_SEND_VIEW_TEMP_INFO);

		// 閲覧情報を設定する。
		List<PageTempView> pageTempViewList = new ArrayList<>();
		List<PokemonTempView> pokemonTempViewList = new ArrayList<>();

		aggregateTargetList.forEach(vti -> {
			// キーを一意にする。
			String uniqueId = UUID.randomUUID().toString();
			if (vti.getPage() != null) {
				String pageName = vti.getPage().name();
				pageTempViewList.add(new PageTempView(pageName + uniqueId, pageName, vti.getIp(), vti.getTime()));
			}
			if (vti.getPokedexId() != null) {
				pokemonTempViewList.add(new PokemonTempView(vti.getPokedexId() + uniqueId, vti.getPokedexId(), vti.getIp(), vti.getTime()));
			}
		});

		pageTempViewRedisRepository.saveAll(pageTempViewList);
		pokemonTempViewRedisRepository.saveAll(pokemonTempViewList);

		log.info(MessageFormat.format(END_MSG_SEND_VIEW_TEMP_INFO, pageTempViewList, pokemonTempViewList));
	}

	/**
	 * 一時閲覧数から加算したPageViewの一覧を生成する。
	 *
	 * @param pageViewMap
	 * @param pageViewList
	 * @param today
	 * @return
	 */
	private List<PageView> createUpdatePageRecords(Map<String, Set<ViewTempInfo>> pageViewMap, List<PageView> pageViewList, Date today) {

		return pageViewMap.entrySet().stream()
				.map(entry -> {
					String page = entry.getKey();
					int viewCount = entry.getValue().size(); // 加算する閲覧数

					Optional<PageView> pvOpt = pageViewList.stream()
							.filter(pvRecords -> page.equals(pvRecords.getPage())).findAny();
					PageView pv;
					if (pvOpt.isPresent()) {
						// 既に閲覧数のレコードが存在する。
						pv = pvOpt.get();
						pv.setViewCount(pv.getViewCount() + viewCount); // 加算する
					} else {
						// 該当のpageのレコードは今日初。
						pv = new PageView();
						pv.setPage(page);
						pv.setYmd(today);
						pv.setViewCount(viewCount); // 閲覧数をセット
					}
					return pv;
				})
				.toList();
	}

	/**
	 * 一時閲覧数から加算したPokemonViewの一覧を生成する。
	 *
	 * @param pokemonViewMap
	 * @param pokemonViewList
	 * @param today
	 * @return
	 */
	private List<PokemonView> createUpdatePokemonRecords(
			Map<String, Set<ViewTempInfo>> pokemonViewMap,
			List<PokemonView> pokemonViewList,
			Date today) {

		return pokemonViewMap.entrySet().stream()
				.map(entry -> {
					String pid = entry.getKey();
					int viewCount = entry.getValue().size(); // 加算する閲覧数

					Optional<PokemonView> pvOpt = pokemonViewList.stream()
							.filter(pvRecords -> pid.equals(pvRecords.getPokedexId())).findAny();
					PokemonView pv;
					if (pvOpt.isPresent()) {
						// 既に閲覧数のレコードが存在する。
						pv = pvOpt.get();
						pv.setViewCount(pv.getViewCount() + viewCount); // 加算する
					} else {
						// 該当のpageのレコードは今日初。
						pv = new PokemonView();
						pv.setPokedexId(pid);
						pv.setYmd(today);
						pv.setViewCount(viewCount); // 閲覧数をセット
					}
					return pv;
				})
				.toList();
	}

	/**
	 * SpringRedisは、なぜかtimeToLiveで削除されたキー名をSet型のオブジェクトから削除してくれない。
	 * これを呼び出すと、それを削除できる。
	 * 
	 * @see PageTempViewRedisRepository
	 */
	void cleanupPageTempView() {
		
		String key = "pageTempView";
		
		List<PageTempView> pageTempViewList = (List<PageTempView>) pageTempViewRedisRepository.findAll();
		List<String> activeIdList = pageTempViewList.stream()
				.filter(ptv -> ptv != null)
				.map(PageTempView::getId)
				.toList();
		
		String[] inactiveIdArr = getInActiveIdArr(key, activeIdList);
		
		if (inactiveIdArr.length == 0) {
			log.info(MessageFormat.format(CLEANUP_NOTHING_INFO, key));
			return;
		}
		
		Long removeCnt = setOperations.remove(key, (Object[]) inactiveIdArr);
		
		log.info(MessageFormat.format(CLEANUP_INFO, key, removeCnt.toString()));
	}

	/**
	 * SpringRedisは、なぜかtimeToLiveで削除されたキー名をSet型のオブジェクトから削除してくれない。
	 * これを呼び出すと、それを削除できる。
	 * 
	 * @see PokemonTempViewRedisRepository
	 */
	void cleanupPokemonTempView() {
		
		String key = "pokemonTempView";
		
		List<PokemonTempView> pokemonTempViewList = (List<PokemonTempView>) pokemonTempViewRedisRepository.findAll();
		List<String> activeIdList = pokemonTempViewList.stream()
				.filter(ptv -> ptv != null)
				.map(PokemonTempView::getId)
				.toList();
		
		String[] inactiveIdArr = getInActiveIdArr(key, activeIdList);
		
		if (inactiveIdArr.length == 0) {
			log.info(MessageFormat.format(CLEANUP_NOTHING_INFO, key));
			return;
		}
		
		Long removeCnt = setOperations.remove(key, (Object[]) inactiveIdArr);
		
		log.info(MessageFormat.format(CLEANUP_INFO, key, removeCnt.toString()));
	}
	
	private String[] getInActiveIdArr(String key, List<String> activeIdList) {
		Set<String> smembers = setOperations.members(key);
		String[] inactiveIdArr = smembers.stream()
				.filter(id -> !activeIdList.contains(id))
				.toArray(String[]::new);
		return inactiveIdArr;
	}

	/**
	 * Redis上のPageTempView(一時的に保持しているページ情報)をすべて削除する。
	 */
	void clearPageTempView() {

		pageTempViewRedisRepository.deleteAll();

		log.info(DELETE_ALL_TEMP_PAGE_INFO);
	}

	/**
	 * Redis上のPokemonTempView(一時的に保持しているポケモン情報)をすべて削除する。
	 */
	void clearPokemonTempView() {

		pokemonTempViewRedisRepository.deleteAll();

		log.info(DELETE_ALL_TEMP_POKEMON_INFO);
	}

}
