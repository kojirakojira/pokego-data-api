package jp.brainjuice.pokego.cache.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.ibm.icu.text.MessageFormat;

import jp.brainjuice.pokego.business.dao.GoPokedexRepository;
import jp.brainjuice.pokego.business.dao.entity.GoPokedex;
import jp.brainjuice.pokego.cache.BjRedisEnum;
import jp.brainjuice.pokego.cache.dao.PageTempViewRedisRepository;
import jp.brainjuice.pokego.cache.dao.PokemonTempViewRedisRepository;
import jp.brainjuice.pokego.cache.dao.entity.PageTempView;
import jp.brainjuice.pokego.cache.dao.entity.PokemonTempView;
import jp.brainjuice.pokego.cache.dao.entity.TempView;
import jp.brainjuice.pokego.cache.inmemory.TopicPageList;
import jp.brainjuice.pokego.cache.inmemory.TopicPokemonList;
import jp.brainjuice.pokego.cache.inmemory.ViewTempInfo;
import jp.brainjuice.pokego.cache.inmemory.ViewTempList;
import jp.brainjuice.pokego.cache.inmemory.data.TopicPage;
import jp.brainjuice.pokego.cache.inmemory.data.TopicPokemon;
import lombok.extern.slf4j.Slf4j;


@Service
@Slf4j
public class ViewsCacheManager {

	private ViewTempList viewTempList;

	private RedisTemplate<String, Integer> redisTemplate;

	private PageTempViewRedisRepository pageTempViewRedisRepository;

	private PokemonTempViewRedisRepository pokemonTempViewRedisRepository;

	private GoPokedexRepository goPokedexRepository;

	private TopicPageList topicPageList;

	private TopicPokemonList topicPokemonList;

	private static final String START_MSG_SCHEDULE = "Start ViewInfo(page, pokemon) schedule.";
	private static final String END_MSG_SCHEDULE = "End ViewInfo(page. pokemon) schedule.";

	private static final String START_MSG_INCR_VIEWS_COUNT_INFO = "Start incr ViewsCount.";
	private static final String END_MSG_INCR_VIEWS_COUNT_INFO = "End incr ViewsCount. page:{0}, pokemon:{1}";

	private static final String START_MSG_SEND_VIEW_TEMP_INFO = "Start send ViewTempInfo.";
	private static final String END_MSG_SEND_VIEW_TEMP_INFO = "End send ViewTempInfo. page:{0}, pokemon:{1}";

	private static final String START_MSG_UPDATE_TOPIC_LIST = "Start update TopicList schedule. TopicPageList: {0}, TopicPokemonList: {1}";
	private static final String END_MSG_UPDATE_TOPIC_LIST = "End update TopicList schedule. TopicPageList: {0}, TopicPokemonList: {1}";

	@Autowired
	public ViewsCacheManager(
			ViewTempList viewTempList,
			RedisTemplate<String, Integer> redisTemplate,
			PageTempViewRedisRepository pageTempViewRedisRepository,
			PokemonTempViewRedisRepository pokemonTempViewRedisRepository,
			GoPokedexRepository goPokedexRepository,
			TopicPokemonList topicPokemonList,
			TopicPageList topicPageList) {
		this.viewTempList = viewTempList;
		this.redisTemplate = redisTemplate;
		this.pageTempViewRedisRepository = pageTempViewRedisRepository;
		this.pokemonTempViewRedisRepository = pokemonTempViewRedisRepository;
		this.goPokedexRepository = goPokedexRepository;
		this.topicPokemonList = topicPokemonList;
		this.topicPageList = topicPageList;
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
	 * （サーバ起動10分後から開始）
	 */
	@Scheduled(initialDelay = 600000, fixedDelay = 900000)
	public void sendViewInfo() {

		log.info(START_MSG_SCHEDULE);

		// 集計対象の閲覧情報の取得
		ArrayList<ViewTempInfo> aggregateTargetList = viewTempList.getAggregateTargetList();

		// 閲覧数の加算
		incrViewsCount(aggregateTargetList);

		// キャッシュサーバへの閲覧情報の一時保存
		sendViewsTempInfo(aggregateTargetList);

		log.info(END_MSG_SCHEDULE);

	}

	/**
	 * キャッシュサーバ(Redisサーバ)上のページ、ポケモンごとの閲覧数を加算する。
	 *
	 * @param aggregateTargetList
	 */
	private void incrViewsCount(ArrayList<ViewTempInfo> aggregateTargetList) {

		log.info(START_MSG_INCR_VIEWS_COUNT_INFO);

		// page閲覧情報リスト、pokemon閲覧情報リストに分割する。
		Map<String, Set<ViewTempInfo>> pageViewMap = new HashMap<>();
		Map<String, Set<ViewTempInfo>> pokemonViewMap = new HashMap<>();

		// Mapのvalueに持つSetに閲覧情報を追加する関数。カリー化（引数 => (Map<String, Set<ViewTempInfo>>, String, ViewTempInfo)）
		Function<Map<String, Set<ViewTempInfo>>, Function<String, Consumer<ViewTempInfo>>> addSetFunc = (map) -> (key) -> (value) -> {
			if (map.containsKey(key)) {
				map.get(key).add(value);
			} else {
				Set<ViewTempInfo> viewSet = new HashSet<>();
				viewSet.add(value);
				map.put(key, viewSet);
			}
		};

		// 集計対象の閲覧情報をMapに設定する。
		aggregateTargetList.forEach(vti -> {
			// ページの閲覧情報をMapに追加する。
			addSetFunc.apply(pageViewMap).apply(vti.getPage()).accept(vti);
			// ポケモンの閲覧情報をMapに追加する。
			addSetFunc.apply(pokemonViewMap).apply(vti.getPokedexId()).accept(vti);

		});

		/** 閲覧数をインクリメント */
		// 現在のRedis上の閲覧数を加算する。
		ValueOperations<String, Integer> vOps = redisTemplate.opsForValue();
		pageViewMap.forEach((k, v) -> {
			vOps.increment(BjRedisEnum.pageViews.name() + k, (long) v.size());
		});
		pokemonViewMap.forEach((k, v) -> {
			vOps.increment(BjRedisEnum.pokemonViews.name() + k, (long) v.size());
		});

		log.info(MessageFormat.format(END_MSG_INCR_VIEWS_COUNT_INFO, pageViewMap, pokemonViewMap));

	}

	/**
	 * ページ、ポケモンの閲覧情報をキャッシュサーバ(Redisサーバ)に送信する。<br>
	 *
	 * @param aggregateTargetList
	 * @see PageTempView
	 * @see PokemonTempView
	 */
	private void sendViewsTempInfo(ArrayList<ViewTempInfo> aggregateTargetList) {

		log.info(START_MSG_SEND_VIEW_TEMP_INFO);

		// 閲覧情報を設定する。
		List<PageTempView> pageTempViewList = new ArrayList<>();
		List<PokemonTempView> pokemonTempViewList = new ArrayList<>();

		aggregateTargetList.forEach(vti -> {
			// キーを一意にする。
			String uniqueId = UUID.randomUUID().toString();
			pageTempViewList.add(new PageTempView(vti.getPage() + uniqueId, vti.getPage(), vti.getIp(), vti.getTime()));
			pokemonTempViewList.add(new PokemonTempView(vti.getPokedexId() + uniqueId, vti.getPokedexId(), vti.getIp(), vti.getTime()));
		});

		pageTempViewRedisRepository.saveAll(pageTempViewList);
		pokemonTempViewRedisRepository.saveAll(pokemonTempViewList);

		log.info(MessageFormat.format(END_MSG_SEND_VIEW_TEMP_INFO, pageTempViewList, pokemonTempViewList));
	}


	/**
	 * 話題のページ、話題のポケモン一覧を更新します。
	 *
	 * 15分おきに実行
	 * タスク実行完了の15分後
	 * （サーバ起動15分後から開始）
	 * @param <T>
	 */
	@Scheduled(initialDelay = 900000, fixedDelay = 900000)
	public void updateTopicList() {

		log.info(MessageFormat.format(START_MSG_UPDATE_TOPIC_LIST, this.topicPageList.toString(), this.topicPokemonList.toString()));

		// TopicPageを更新する。
		ArrayList<TopicPage> topicPageList = getTopicPageList();
		this.topicPageList.clear();
		this.topicPageList.addAll(topicPageList);

		// TopicPokemonを更新する。
		ArrayList<TopicPokemon> topicPokemonList = getTopicPokemonList();
		this.topicPokemonList.clear();
		this.topicPokemonList.addAll(topicPokemonList);

		log.info(MessageFormat.format(END_MSG_UPDATE_TOPIC_LIST, topicPageList.toString(), topicPokemonList.toString()));
	}


	/**
	 * TopicPageのリストを取得します。<br>
	 * 閲覧数の降順で取得します。
	 *
	 * @return
	 */
	private ArrayList<TopicPage> getTopicPageList() {

		ArrayList<TopicPage> topicPageList = new ArrayList<>();

		// Redisから検索
		Iterable<PageTempView> pageTempViewList = pageTempViewRedisRepository.findAll();

		// pageごとの閲覧数をマップで取得
		Map<String, Integer> pageViewsMap = createViewsCountMap(pageTempViewList);

		// TopicPageの生成
		pageViewsMap.forEach((k, v) -> {
			topicPageList.add(new TopicPage(k, null, v));
		});

		// 並び替え（降順）
		Collections.sort(topicPageList, (o1, o2) -> {
			return o1.getCount() < o2.getCount() ? -1 : 1;
		});

		return topicPageList;
	}

	/**
	 * TopicPokemonのリストを取得します。<br>
	 * 閲覧数の降順で取得します。
	 *
	 * @return
	 */
	private ArrayList<TopicPokemon> getTopicPokemonList() {

		ArrayList<TopicPokemon> topicPokemonList = new ArrayList<>();

		// Redisから検索
		Iterable<PokemonTempView> pokemonTempViewList = pokemonTempViewRedisRepository.findAll();

		// pokemonごとの閲覧数をマップで取得
		Map<String, Integer> pokemonViewsMap = createViewsCountMap(pokemonTempViewList);

		// GoPokedexのリストを取得
		Iterable<GoPokedex> goPokedexList = goPokedexRepository.findAllById(pokemonViewsMap.keySet());

		// TopicPokemonの生成
		pokemonViewsMap.forEach((k, v) -> {
			for (GoPokedex gp: goPokedexList) {
				if (k.equals(gp.getPokedexId()))
					topicPokemonList.add(new TopicPokemon(gp.getPokedexId(), gp.getName(), gp.getImage(), v));
			}
		});

		// 並び替え（降順）
		Collections.sort(topicPokemonList, (o1, o2) -> {
			return o1.getCount() < o2.getCount() ? -1 : 1;
		});

		return topicPokemonList;
	}

	/**
	 * keyごとの閲覧数を保持したマップを生成します。<br>
	 * ここでいうkeyは、TempViewのメンバ変数を指します。
	 *
	 * @param <S>
	 * @param tempViewList
	 * @return
	 */
	private <S extends TempView> Map<String, Integer> createViewsCountMap(Iterable<S> tempViewList) {

		Map<String, Integer> viewsMap = new HashMap<String, Integer>();

		// 閲覧数をインクリメントしていくConsumer
		BiConsumer<Map<String, Integer>, String> countView = (map, key) -> {
			if (map.containsKey(key)) {
				map.put(key, map.get(key) + 1);
			} else {
				map.put(key, 1);
			}
		};

		tempViewList.forEach(tv -> {
			countView.accept(viewsMap, tv.getKey());
		});

		return viewsMap;
	}

}
