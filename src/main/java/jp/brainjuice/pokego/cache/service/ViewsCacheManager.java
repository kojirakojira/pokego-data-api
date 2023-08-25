package jp.brainjuice.pokego.cache.service;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jp.brainjuice.pokego.business.service.utils.PokemonEditUtils;
import jp.brainjuice.pokego.cache.BjRedisEnum;
import jp.brainjuice.pokego.cache.dao.PageTempViewRedisRepository;
import jp.brainjuice.pokego.cache.dao.PokemonTempViewRedisRepository;
import jp.brainjuice.pokego.cache.dao.entity.PageTempView;
import jp.brainjuice.pokego.cache.dao.entity.PokemonTempView;
import jp.brainjuice.pokego.cache.inmemory.ViewTempInfo;
import jp.brainjuice.pokego.cache.inmemory.ViewTempList;
import jp.brainjuice.pokego.cache.inmemory.data.PageNameEnum;
import lombok.extern.slf4j.Slf4j;

/**
 * Redisサーバ上の閲覧情報を管理するためのクラスです。
 *
 * @author saibabanagchampa
 *
 */
@Component
@Slf4j
public class ViewsCacheManager {

	private ViewTempList viewTempList;

	private RedisTemplate<String, String> redisTemplate;

	/** Redis上の一時的なページ閲覧情報を管理するためのリポジトリ */
	private PageTempViewRedisRepository pageTempViewRedisRepository;
	/** Redis上の一時的なポケモン閲覧情報を管理するためのリポジトリ */
	private PokemonTempViewRedisRepository pokemonTempViewRedisRepository;

	private static final String START_MSG_SCHEDULE = "Start ViewInfo(page, pokemon) schedule.";
	private static final String END_MSG_SCHEDULE = "End ViewInfo(page. pokemon) schedule.";

	private static final String START_MSG_INCR_VIEWS_COUNT_INFO = "> Start incr ViewsCount.";
	private static final String END_MSG_INCR_VIEWS_COUNT_INFO = "> End incr ViewsCount. page:{0}, pokemon:{1}";

	private static final String START_MSG_SEND_VIEW_TEMP_INFO = "> Start send ViewTempInfo.";
	private static final String END_MSG_SEND_VIEW_TEMP_INFO = "> End send ViewTempInfo. page:{0}, pokemon:{1}";

	private static final String DELETE_ALL_TEMP_PAGE_INFO = "Delete All PageTempView.";
	private static final String DELETE_ALL_TEMP_POKEMON_INFO = "Delete All PokemonTempView.";

	@Autowired
	public ViewsCacheManager(
			ViewTempList viewTempList,
			RedisTemplate<String, String> redisTemplate,
			PageTempViewRedisRepository pageTempViewRedisRepository,
			PokemonTempViewRedisRepository pokemonTempViewRedisRepository) {
		this.viewTempList = viewTempList;
		this.redisTemplate = redisTemplate;
		this.pageTempViewRedisRepository = pageTempViewRedisRepository;
		this.pokemonTempViewRedisRepository = pokemonTempViewRedisRepository;
	}

	/**
	 * ページごとの閲覧数をRedisサーバからすべて取得します。
	 *
	 * @return
	 */
	Map<PageNameEnum, Integer> findPageViewsAll() {

		// キーの一覧を取得する(command="keys pageViews:*")
		Set<String> pageViewKeys = redisTemplate.keys(BjRedisEnum.pageViews.name().concat(":*"));

		Map<String, Integer> rtnMap = getViewsMap(pageViewKeys);

		// キーをString型からPageNameEnum型に変換して返却する。
		return rtnMap.entrySet()
				.stream()
				.collect(Collectors.toMap(
						entry -> PageNameEnum.valueOf(entry.getKey()),
						Map.Entry::getValue));
	}

	/**
	 * ポケモンごとの閲覧数をRedisサーバからすべて取得します。
	 *
	 * @return
	 */
	Map<String, Integer> findPokemonViewsAll() {

		// キーの一覧を取得する(command="keys pokemonViews:*")
		Set<String> pageViewKeys = redisTemplate.keys(BjRedisEnum.pokemonViews.name().concat(":*"));

		Map<String, Integer> rtnMap = getViewsMap(pageViewKeys);

		return rtnMap;
	}

	/**
	 * Keyの一覧をもとにRedisサーバからValueを取得します。
	 *
	 * @param pageViewKeys
	 * @return
	 */
	private Map<String, Integer> getViewsMap(Set<String> pageViewKeys) {

		Map<String, Integer> rtnMap = new HashMap<>();

		// Keyのリスト（順番を担保）
		List<String> keyList = new ArrayList<String>(pageViewKeys);
		// Valueのリスト（multiGetはコレクションの順番ごとに取得される。返却値の型はArrayList。）
		// command="mget key1 key2..."
		List<String> views = redisTemplate.opsForValue().multiGet(pageViewKeys);

		// 順番ごとに2つのリストをループさせMapを作成する。
		for (int i = 0; i < keyList.size(); i++) {
			rtnMap.put(keyList.get(i), Integer.parseInt(views.get(i)));
		}

		return rtnMap;
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

		// 閲覧数の加算
		incrViewsCount(aggregateTargetList);

		// キャッシュサーバへの閲覧情報の一時保存
		sendViewsTempInfo(aggregateTargetList);

		log.info(END_MSG_SCHEDULE);

	}

	/**
	 * キャッシュサーバ(Redisサーバ)上のページ、ポケモンごとの閲覧数を加算する。
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

		/** 閲覧数をインクリメント */
		// 現在のRedis上の閲覧数を加算する。
		ValueOperations<String, String> vOps = redisTemplate.opsForValue();
		pageViewMap.forEach((k, v) -> {
			vOps.increment(BjRedisEnum.pageViews.name() + ":" + k, (long) v.size());
		});
		pokemonViewMap.forEach((k, v) -> {
			vOps.increment(BjRedisEnum.pokemonViews.name() + ":" + k, (long) v.size());
		});

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
