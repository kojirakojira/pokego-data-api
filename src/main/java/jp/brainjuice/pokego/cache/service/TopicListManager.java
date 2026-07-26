package jp.brainjuice.pokego.cache.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import jp.brainjuice.pokego.business.service.search.utils.PokemonEditUtils;
import jp.brainjuice.pokego.cache.inmemory.topic.data.PageNameEnum;
import jp.brainjuice.pokego.cache.inmemory.topic.data.TopicPage;
import jp.brainjuice.pokego.cache.inmemory.topic.data.TopicPokemon;
import jp.brainjuice.pokego.dao.jpa.GoPokedexRepository;
import jp.brainjuice.pokego.dao.jpa.entity.GoPokedex;
import jp.brainjuice.pokego.utils.BjUtils;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;

/**
 * 話題の○○のリストを管理するクラスです。<br>
 * Redisサーバからメモリ上のTopic○○Listに反映させます。
 *
 * @author saibabanagchampa
 * @see ViewsCacheProvider
 * @see ViewsCacheManager
 */
@Component
@Slf4j
public class TopicListManager {

	private StringRedisTemplate redisTemplate;
	private GoPokedexRepository goPokedexRepository;
	private ObjectMapper objectMapper;

	// 過去の集計時間
	public static final int EXPIRE_HOURS = 72;
	// TopicPageリストの上位表示件数
	public static final int TOPIC_PAGE_LIMIT = 10;
	// TopicPokemonリストの上位表示件数
	public static final int TOPIC_POKEMON_LIMIT = 10;

	private static final String CACHE_TOPIC_PAGE = "cache:topic:page";
	private static final String CACHE_TOPIC_POKEMON = "cache:topic:pokemon";
	private static final String TRENDING_PAGE_PREFIX = "trending:page:";
	private static final String TRENDING_POKEMON_PREFIX = "trending:pokemon:";
	private static final String TRENDING_PAGE_UNION = "trending:page:union_hours";
	private static final String TRENDING_POKEMON_UNION = "trending:pokemon:union_hours";

	public TopicListManager(
			StringRedisTemplate redisTemplate,
			GoPokedexRepository goPokedexRepository,
			ObjectMapper objectMapper) {
		this.redisTemplate = redisTemplate;
		this.goPokedexRepository = goPokedexRepository;
		this.objectMapper = objectMapper;
	}

	/**
	 * Redisサーバ上の情報から、話題のページ(TopicPage)、話題のポケモン(TopicPokemon)の一覧を更新します。<br>
	 *
	 * 10分おき（毎時0分, 10分, 20分, 30分, 40分, 50分）に実行
	 */
	@Scheduled(cron = "0 0/10 * * * ?")
	@SchedulerLock(name = "topic_list_sync_lock", lockAtMostFor = "PT9M", lockAtLeastFor = "PT9M")
	public void updateTopicList() {
		log.info("Start update TopicList schedule(Redis -> Redis JSON)");

		try {
			// TopicPageを更新する。
			List<TopicPage> updatedTopicPageList = createTopicPageList();
			redisTemplate.opsForValue().set(CACHE_TOPIC_PAGE, objectMapper.writeValueAsString(updatedTopicPageList));

			// TopicPokemonを更新する。
			List<TopicPokemon> updatedTopicPokemonList = createTopicPokemonList();
			redisTemplate.opsForValue().set(CACHE_TOPIC_POKEMON,
					objectMapper.writeValueAsString(updatedTopicPokemonList));

		} catch (Exception e) {
			log.error("Failed to update TopicList to Redis JSON", e);
		}

		log.info("End update TopicList schedule.");
	}

	/**
	 * 過去${EXPIRE_HOURS}時間の時間別ZSETのキーリストを生成する。
	 */
	private List<String> getUntilExpiredHoursKeys(String prefix) {
		List<String> keys = new ArrayList<>();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHH");
		LocalDateTime now = BjUtils.nowLocalDateTime();
		for (int i = 0; i < EXPIRE_HOURS; i++) {
			keys.add(prefix + now.minusHours(i).format(formatter));
		}
		return keys;
	}

	/**
	 * TopicPageのリストを生成します。<br>
	 * 閲覧数の降順で取得します。<br>
	 * 上位${TOPIC_PAGE_LIMIT}件のみ取得します。
	 *
	 * @return
	 */
	private List<TopicPage> createTopicPageList() {
		List<String> keys = getUntilExpiredHoursKeys(TRENDING_PAGE_PREFIX);
		if (keys.isEmpty())
			return Collections.emptyList();

		String unionKey = TRENDING_PAGE_UNION;
		String firstKey = keys.get(0);
		List<String> otherKeys = keys.subList(1, keys.size());

		// 過去${EXPIRE_HOURS}時間のスコアを合算し、Redis上にZSetとして保存
		redisTemplate.opsForZSet().unionAndStore(firstKey, otherKeys, unionKey);
		// 万が一のクラッシュに備えたフェイルセーフとして5分のTTLを設定
		redisTemplate.expire(unionKey, 5, TimeUnit.MINUTES);

		// 降順で並び替え、上位${TOPIC_PAGE_LIMIT}件のみを取得する（0〜9）
		Set<TypedTuple<String>> topPages = redisTemplate.opsForZSet()
				.reverseRangeWithScores(unionKey, 0, TOPIC_PAGE_LIMIT - 1);

		// 一時キーを削除してメモリを解放する
		redisTemplate.delete(unionKey);

		if (topPages == null || topPages.isEmpty())
			return Collections.emptyList();

		return topPages.stream()
				.map(tuple -> {
					PageNameEnum pageName = PageNameEnum.valueOf(tuple.getValue());
					return new TopicPage(pageName, pageName.getJpn(), tuple.getScore().intValue());
				})
				.collect(Collectors.toList());
	}

	/**
	 * TopicPokemonのリストを生成します。<br>
	 * 閲覧数の降順で取得します。<br>
	 * 上位${TOPIC_POKEMON_LIMIT}件のみ取得します。
	 *
	 * @return
	 */
	private List<TopicPokemon> createTopicPokemonList() {
		List<String> keys = getUntilExpiredHoursKeys(TRENDING_POKEMON_PREFIX);
		if (keys.isEmpty())
			return Collections.emptyList();

		String unionKey = TRENDING_POKEMON_UNION;
		String firstKey = keys.get(0);
		List<String> otherKeys = keys.subList(1, keys.size());

		// 過去${EXPIRE_HOURS}時間のスコアを合算し、Redis上にZSetとして保存
		redisTemplate.opsForZSet().unionAndStore(firstKey, otherKeys, unionKey);
		// 万が一のクラッシュに備えたフェイルセーフとして5分のTTLを設定
		redisTemplate.expire(unionKey, 5, TimeUnit.MINUTES);

		// 降順で並び替え、上位${TOPIC_POKEMON_LIMIT}件のみを取得する（0〜9）
		Set<TypedTuple<String>> topPokemons = redisTemplate.opsForZSet()
				.reverseRangeWithScores(unionKey, 0, TOPIC_POKEMON_LIMIT - 1);

		// 一時キーを削除してメモリを解放する
		redisTemplate.delete(unionKey);

		if (topPokemons == null || topPokemons.isEmpty())
			return Collections.emptyList();

		Set<String> pokedexIds = topPokemons.stream()
				.map(TypedTuple::getValue)
				.collect(Collectors.toSet());

		Map<String, GoPokedex> goPokedexMap = goPokedexRepository.findAllById(pokedexIds).stream()
				.collect(Collectors.toMap(GoPokedex::getPokedexId, gp -> gp));

		return topPokemons.stream()
				.map(tuple -> {
					GoPokedex gp = goPokedexMap.get(tuple.getValue());
					if (gp == null)
						return null;
					return new TopicPokemon(
							gp.getPokedexId(),
							gp.getImage1(),
							PokemonEditUtils.appendRemarks(gp),
							tuple.getScore().intValue()); // TopicPokemonに変換
				})
				.filter(tp -> tp != null)
				.collect(Collectors.toList());
	}
}
