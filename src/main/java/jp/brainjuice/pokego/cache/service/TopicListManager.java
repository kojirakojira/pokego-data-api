package jp.brainjuice.pokego.cache.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.ibm.icu.text.MessageFormat;

import jp.brainjuice.pokego.business.dao.GoPokedexRepository;
import jp.brainjuice.pokego.business.dao.entity.GoPokedex;
import jp.brainjuice.pokego.cache.dao.PageTempViewRedisRepository;
import jp.brainjuice.pokego.cache.dao.PokemonTempViewRedisRepository;
import jp.brainjuice.pokego.cache.dao.entity.PageTempView;
import jp.brainjuice.pokego.cache.dao.entity.PokemonTempView;
import jp.brainjuice.pokego.cache.dao.entity.TempView;
import jp.brainjuice.pokego.cache.inmemory.TopicPageList;
import jp.brainjuice.pokego.cache.inmemory.TopicPokemonList;
import jp.brainjuice.pokego.cache.inmemory.data.PageNameEnum;
import jp.brainjuice.pokego.cache.inmemory.data.TopicPage;
import jp.brainjuice.pokego.cache.inmemory.data.TopicPokemon;
import lombok.extern.slf4j.Slf4j;

/**
 * 話題の○○のリストを管理するクラスです。<br>
 * Redisサーバからメモリ上のTopic○○に反映させます。
 *
 * @author saibabanagchampa
 *
 */
@Component
@Slf4j
public class TopicListManager {

	private PageTempViewRedisRepository pageTempViewRedisRepository;

	private PokemonTempViewRedisRepository pokemonTempViewRedisRepository;

	private GoPokedexRepository goPokedexRepository;

	private TopicPageList topicPageList;

	private TopicPokemonList topicPokemonList;

	private static final String START_MSG_UPDATE_TOPIC_LIST = "Start update TopicList schedule. TopicPageList: {0}, TopicPokemonList: {1}";
	private static final String END_MSG_UPDATE_TOPIC_LIST = "End update TopicList schedule. TopicPageList: {0}, TopicPokemonList: {1}";

	private static final String MSG_TEMP_VIEW_LIST = "> TempViewList: {0}";

	@Autowired
	public TopicListManager(
			PageTempViewRedisRepository pageTempViewRedisRepository,
			PokemonTempViewRedisRepository pokemonTempViewRedisRepository,
			GoPokedexRepository goPokedexRepository,
			TopicPokemonList topicPokemonList,
			TopicPageList topicPageList) {
		this.pageTempViewRedisRepository = pageTempViewRedisRepository;
		this.pokemonTempViewRedisRepository = pokemonTempViewRedisRepository;
		this.goPokedexRepository = goPokedexRepository;
		this.topicPokemonList = topicPokemonList;
		this.topicPageList = topicPageList;
	}

	/**
	 * 話題のページ（検索パターン）を取得する。
	 *
	 * @return
	 */
	TopicPageList getTopicPageList() {
		return topicPageList;
	}

	/**
	 * 話題のポケモンを取得する。
	 *
	 * @return
	 */
	TopicPokemonList getTopicPokemonList() {
		return topicPokemonList;
	}

	/**
	 * Redisサーバ上の情報から、メモリ上の話題のページ(TopicPage)、話題のポケモン(TopicPokemon)の一覧を更新します。<br>
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
		ArrayList<TopicPage> topicPageList = createTopicPageList();
		this.topicPageList.clear();
		this.topicPageList.addAll(topicPageList);

		// TopicPokemonを更新する。
		ArrayList<TopicPokemon> topicPokemonList = createTopicPokemonList();
		this.topicPokemonList.clear();
		this.topicPokemonList.addAll(topicPokemonList);

		log.info(MessageFormat.format(END_MSG_UPDATE_TOPIC_LIST, this.topicPageList.toString(), this.topicPokemonList.toString()));
	}


	/**
	 * TopicPageのリストを生成します。<br>
	 * 閲覧数の降順で取得します。
	 *
	 * @return
	 */
	private ArrayList<TopicPage> createTopicPageList() {

		ArrayList<TopicPage> topicPageList = new ArrayList<>();

		// Redisから検索
		Iterable<PageTempView> viewsCountMap = pageTempViewRedisRepository.findAll();

		// pageごとの閲覧数をマップで取得
		Map<String, Integer> pageViewsMap = createViewsCountMap(viewsCountMap);

		// TopicPageの生成
		pageViewsMap.forEach((k, v) -> {
			PageNameEnum pageName = PageNameEnum.valueOf(k);
			topicPageList.add(new TopicPage(pageName, pageName.getJpn(), v));
		});

		// 並び替え（降順）
		Collections.sort(topicPageList, (o1, o2) -> {
			return o1.getCount() < o2.getCount() ? 1 : -1;
		});

		return topicPageList;
	}

	/**
	 * TopicPokemonのリストを生成します。<br>
	 * 閲覧数の降順で取得します。
	 *
	 * @return
	 */
	private ArrayList<TopicPokemon> createTopicPokemonList() {

		ArrayList<TopicPokemon> topicPokemonList = new ArrayList<>();

		// Redisから検索
		Iterable<PokemonTempView> pokemonTempViewList = pokemonTempViewRedisRepository.findAll();

		// pokemonごとの閲覧数をマップで取得
		Map<String, Integer> viewsCountMap = createViewsCountMap(pokemonTempViewList);

		// GoPokedexのリストを取得
		Iterable<GoPokedex> goPokedexList = goPokedexRepository.findAllById(viewsCountMap.keySet());

		// TopicPokemonの生成
		viewsCountMap.forEach((k, v) -> {
			for (GoPokedex gp: goPokedexList) {
				if (k.equals(gp.getPokedexId())) {
					topicPokemonList.add(new TopicPokemon(gp.getPokedexId(), gp.getName(), v));
				}
			}
		});

		// 並び替え（降順）
		Collections.sort(topicPokemonList, (o1, o2) -> {
			return o1.getCount() < o2.getCount() ? 1 : -1;
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

		log.info(MessageFormat.format(MSG_TEMP_VIEW_LIST, tempViewList.toString()));

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
			// TODO: Spring Data Redisのバグのためnullチェック。（https://techhelpnotes.com/java-spring-boot-redis-crud-repository-findbyid-or-findall-always-returns-optional-empty-null/）
			if (tv != null) {
				countView.accept(viewsMap, tv.getKey());
			}
		});

		return viewsMap;
	}
}
