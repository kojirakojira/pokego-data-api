package jp.brainjuice.pokego.cache.service;

import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

import org.springframework.data.redis.core.StringRedisTemplate;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jp.brainjuice.pokego.cache.inmemory.topic.data.TopicPage;
import jp.brainjuice.pokego.cache.inmemory.topic.data.TopicPokemon;
import lombok.extern.slf4j.Slf4j;

/**
 * TopicListManagerにアクセスするためのプロバイダクラスです。<br>
 * Redisサーバからメモリ上のTopic○○ListWに反映させます。
 *
 * @author saibabanagchampa
 * @see TopicListManager
 */
@Service
@Slf4j
public class TopicListProvider {

	private TopicListManager topicListManager;
	private StringRedisTemplate redisTemplate;
	private ObjectMapper objectMapper;

	public TopicListProvider(TopicListManager topicListManager, StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
		this.topicListManager = topicListManager;
		this.redisTemplate = redisTemplate;
		this.objectMapper = objectMapper;
	}

	/**
	 * 話題のページ（検索パターン）を取得する。
	 *
	 * @return
	 */
	public List<TopicPage> getTopicPageList() {
		String json = redisTemplate.opsForValue().get("cache:topic:page");
		if (json != null) {
			try {
				return objectMapper.readValue(json, new TypeReference<List<TopicPage>>() {});
			} catch (Exception e) {
				log.error("Failed to parse topic page list from redis", e);
			}
		}
		// キャッシュが無い場合は空リストを返す
		return Collections.emptyList();
	}

	/**
	 * 話題のポケモンを取得する。
	 *
	 * @return
	 */
	public List<TopicPokemon> getTopicPokemonList() {
		String json = redisTemplate.opsForValue().get("cache:topic:pokemon");
		if (json != null) {
			try {
				return objectMapper.readValue(json, new TypeReference<List<TopicPokemon>>() {});
			} catch (Exception e) {
				log.error("Failed to parse topic pokemon list from redis", e);
			}
		}
		// キャッシュが無い場合は空リストを返す
		return Collections.emptyList();
	}

	/**
	 * TopicListを強制的に更新させる。（普通はスケジュール実行）
	 *
	 */
	public void updateTopicList() {
		topicListManager.updateTopicList();
	}
}
