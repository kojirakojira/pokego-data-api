package jp.brainjuice.pokego.cache.service;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jp.brainjuice.pokego.business.dao.GoPokedexRepository;
import jp.brainjuice.pokego.business.dao.entity.GoPokedex;
import jp.brainjuice.pokego.business.service.utils.PokemonEditUtils;
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
 * @see ViewsCacheProvider
 * @see ViewsCacheManager
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
	 * （サーバ起動30秒後から開始）
	 * @param <T>
	 */
	@Scheduled(initialDelay = 30000, fixedDelay = 900000)
	public void updateTopicList() {

		log.info(MessageFormat.format(START_MSG_UPDATE_TOPIC_LIST, this.topicPageList.toString(), this.topicPokemonList.toString()));

		// TopicPageを更新する。
		List<TopicPage> topicPageList = createTopicPageList();
		this.topicPageList.setAll(topicPageList);

		// TopicPokemonを更新する。
		List<TopicPokemon> topicPokemonList = createTopicPokemonList();
		this.topicPokemonList.setAll(topicPokemonList);

		log.info(MessageFormat.format(END_MSG_UPDATE_TOPIC_LIST, this.topicPageList.toString(), this.topicPokemonList.toString()));
	}


	/**
	 * TopicPageのリストを生成します。<br>
	 * 閲覧数の降順で取得します。
	 *
	 * @return
	 */
	private List<TopicPage> createTopicPageList() {

		// Redisから検索
		Iterable<PageTempView> viewsCountMap = pageTempViewRedisRepository.findAll();

		// pageごとの閲覧数をマップで取得
		Map<String, Integer> pageViewsMap = createViewsCountMap(viewsCountMap);

		// TopicPageのリストを生成
		return pageViewsMap.entrySet().stream()
				.map(entry -> {
					PageNameEnum pageName = PageNameEnum.valueOf(entry.getKey());
					return new TopicPage(pageName, pageName.getJpn(), entry.getValue());
				})
				.filter(tp -> tp.getPage() != PageNameEnum.abundance) // abundanceは検索ページではないため、対象外とする。
				.sorted((o1, o2) -> {
					return o1.getCount() < o2.getCount() ? 1 : -1;
				})
				.collect(Collectors.toList());
	}

	/**
	 * TopicPokemonのリストを生成します。<br>
	 * 閲覧数の降順で取得します。
	 *
	 * @return
	 */
	private List<TopicPokemon> createTopicPokemonList() {

		// Redisから検索
		Iterable<PokemonTempView> pokemonTempViewList = pokemonTempViewRedisRepository.findAll();

		// pokemonごとの閲覧数をマップで取得
		Map<String, Integer> viewsCountMap = createViewsCountMap(pokemonTempViewList);

		// GoPokedexのリストを取得
		Map<String, GoPokedex> goPokedexMap = goPokedexRepository.findAllById(viewsCountMap.keySet()).stream()
				.collect(Collectors.toMap(
						GoPokedex::getPokedexId,
						gp -> gp));

		// TopicPokemonのリストを生成
		return viewsCountMap.entrySet().stream()
				.map(entry -> {
					GoPokedex gp = goPokedexMap.get(entry.getKey());
					return new TopicPokemon(
							gp.getPokedexId(),
							gp.getImage(),
							PokemonEditUtils.appendRemarks(gp),
							entry.getValue()); // TopicPokemonに変換。
				})
				.sorted((o1, o2) -> {
					return o1.getCount() < o2.getCount() ? 1 : -1;
				}) // 並び替え
				.collect(Collectors.toList());
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
