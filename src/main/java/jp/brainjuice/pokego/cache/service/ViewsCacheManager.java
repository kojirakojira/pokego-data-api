package jp.brainjuice.pokego.cache.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.ibm.icu.text.MessageFormat;

import jp.brainjuice.pokego.cache.dao.PageViewRedisRepository;
import jp.brainjuice.pokego.cache.dao.PokemonViewRedisRepository;
import jp.brainjuice.pokego.cache.dao.entity.PageViewInfo;
import jp.brainjuice.pokego.cache.dao.entity.PokemonViewInfo;
import jp.brainjuice.pokego.cache.inmemory.TempTopicPokemonList;
import jp.brainjuice.pokego.cache.inmemory.ViewTempInfo;
import jp.brainjuice.pokego.cache.inmemory.ViewTempList;
import lombok.extern.slf4j.Slf4j;


@Service
@Slf4j
public class ViewsCacheManager {

	private ViewTempList viewTempList;

	private PageViewRedisRepository pageViewRedisRepository;

	private PokemonViewRedisRepository pokemonViewRedisRepository;

	private RedisTemplate<String, Integer> redisTemplate;

	private TempTopicPokemonList tempTopicEventList;

	private static final String SCHEDULE_MESSAGE_START = "Start sending ViewInfo(page, pokemon).";

	private static final String SCHEDULE_MESSAGE_END = "End sending ViewInfo(page. pokemon) PageViewInfo = {0}, PokemonViewInfo = {1}";

	@Autowired
	public ViewsCacheManager(
			ViewTempList viewTempList,
			PageViewRedisRepository pageViewRedisRepository,
			PokemonViewRedisRepository pokemonViewRedisRepository,
			RedisTemplate<String, Integer> redisTemplate,
			TempTopicPokemonList tempTopicEventList) {
		this.viewTempList = viewTempList;
		this.pageViewRedisRepository = pageViewRedisRepository;
		this.pokemonViewRedisRepository = pokemonViewRedisRepository;
		this.redisTemplate = redisTemplate;
		this.tempTopicEventList = tempTopicEventList;
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

		log.info(SCHEDULE_MESSAGE_START);

		// 集計対象の閲覧情報の取得
		ArrayList<ViewTempInfo> aggregateTargetList = viewTempList.getAggregateTargetList();

		// page閲覧情報リスト、pokemon閲覧情報リストに分割する。
		List<PageViewInfo> pageViewInfoList = new ArrayList<PageViewInfo>();
		List<PokemonViewInfo> pokemonViewInfoList = new ArrayList<PokemonViewInfo>();
		aggregateTargetList.forEach(vti -> {
			// page閲覧情報リストに追加
			pageViewInfoList.add(new PageViewInfo(vti.getPage(), vti.getIp(), vti.getTime()));
			// pokemon閲覧情報リストに追加
			pokemonViewInfoList.add(new PokemonViewInfo(vti.getPokedexId(), vti.getIp(), vti.getTime()));
		});

		// ページ
		pageViewRedisRepository.saveAll(pageViewInfoList);
		// ポケモン
		pokemonViewRedisRepository.saveAll(pokemonViewInfoList);

		log.info(MessageFormat.format(SCHEDULE_MESSAGE_END, pageViewInfoList, pokemonViewInfoList));

	}

}
