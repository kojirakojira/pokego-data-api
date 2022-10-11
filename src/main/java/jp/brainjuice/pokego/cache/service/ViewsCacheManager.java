package jp.brainjuice.pokego.cache.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.ibm.icu.text.MessageFormat;

import jp.brainjuice.pokego.cache.BjRedisEnum;
import jp.brainjuice.pokego.cache.dao.PageTempViewRedisRepository;
import jp.brainjuice.pokego.cache.dao.PokemonTempViewRedisRepository;
import jp.brainjuice.pokego.cache.dao.entity.PageTempView;
import jp.brainjuice.pokego.cache.dao.entity.PokemonTempView;
import jp.brainjuice.pokego.cache.inmemory.TopicPageList;
import jp.brainjuice.pokego.cache.inmemory.TopicPokemonList;
import jp.brainjuice.pokego.cache.inmemory.ViewTempInfo;
import jp.brainjuice.pokego.cache.inmemory.ViewTempList;
import lombok.extern.slf4j.Slf4j;


@Service
@Slf4j
public class ViewsCacheManager {

	private ViewTempList viewTempList;

	private RedisTemplate<String, Integer> redisTemplate;

	private PageTempViewRedisRepository pageTempViewRedisRepository;

	private PokemonTempViewRedisRepository pokemonTempViewRedisRepository;

	private TopicPokemonList topicPokemonList;

	private TopicPageList topicPageList;

	private static final String START_MSG_SCHEDULE = "Start sending ViewInfo(page, pokemon).";
	private static final String END_MSG_SCHEDULE = "End sending ViewInfo(page. pokemon).";

	private static final String START_MSG_INCR_VIEWS_COUNT_INFO = "Start incr ViewsCount.";
	private static final String END_MSG_INCR_VIEWS_COUNT_INFO = "End incr ViewsCount. page:{0}, pokemon:{1}";

	private static final String START_MSG_SEND_VIEW_TEMP_INFO_MSG = "Start send ViewTempInfo.";
	private static final String END_MSG_SEND_VIEW_TEMP_INFO_MSG = "End send ViewTempInfo. page:{0}, pokemon:{1}";

	@Autowired
	public ViewsCacheManager(
			ViewTempList viewTempList,
			PageTempViewRedisRepository pageTempViewRedisRepository,
			PokemonTempViewRedisRepository pokemonTempViewRedisRepository,
			RedisTemplate<String, Integer> redisTemplate,
			TopicPokemonList topicPokemonList,
			TopicPageList topicPageList) {
		this.viewTempList = viewTempList;
		this.pageTempViewRedisRepository = pageTempViewRedisRepository;
		this.pokemonTempViewRedisRepository = pokemonTempViewRedisRepository;
		this.redisTemplate = redisTemplate;
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
		// Key: ページ, Value: "IPアドレス(Dateのlong型の値)"のセット
		Map<String, Set<ViewTempInfo>> pageViewMap = new HashMap<>();
		Map<String, Set<ViewTempInfo>> pokemonViewMap = new HashMap<>();

		// Mapのvalueに持つSetに閲覧情報を追加する関数。カリー化（引数 => (Map<String, Set<String>>, String, ViewTempInfo)）
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

		log.info(START_MSG_SEND_VIEW_TEMP_INFO_MSG);

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

		log.info(MessageFormat.format(END_MSG_SEND_VIEW_TEMP_INFO_MSG, pageTempViewList, pokemonTempViewList));
	}

}
