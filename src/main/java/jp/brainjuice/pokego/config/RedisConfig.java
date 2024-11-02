package jp.brainjuice.pokego.config;

import java.text.MessageFormat;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.data.redis.LettuceClientConfigurationBuilderCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import lombok.extern.slf4j.Slf4j;

@Configuration
@EnableCaching
@EnableRedisRepositories(basePackages = { "jp.brainjuice.pokego.cache.dao.redis" })
@Slf4j
public class RedisConfig {

	private static final String CONNECTED_MESSAGE_FORMAT = "Redis Configuration Builder start. REDIS_URL={0}";

	public RedisConfig(@Value("${redis.env.url}") String envUrl) {
		log.info(MessageFormat.format(CONNECTED_MESSAGE_FORMAT, envUrl));
		log.info(MessageFormat.format("Redis Configuration Builder start. REDIS_TLS_URL={0}", System.getenv("REDIS_TLS_URL")));
		log.info(MessageFormat.format("Redis Configuration Builder start. REDIS_TEMPORARY_URL={0}", System.getenv("REDIS_TEMPORARY_URL")));
	}


	/**
	 * デフォルトキャッシュ設定
	 *
	 * @return
	 */
	@Bean
	RedisCacheConfiguration cacheConfiguration() {
		return RedisCacheConfiguration
				.defaultCacheConfig()
				// TODO: Spring Data Redisのバグのため効いていないっぽい。（https://techhelpnotes.com/java-spring-boot-redis-crud-repository-findbyid-or-findall-always-returns-optional-empty-null/）
				.disableCachingNullValues()
				.serializeValuesWith(SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));
	}

	/**
	 * キャッシュ設定のカスタマイズ
	 *
	 * @return
	 */
//	@Bean
//	public RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer() {
//		return (builder) -> builder
//				// イベント詳細画面のキャッシュ
//				.withCacheConfiguration(BjCacheEnum.eventCache.name(), RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofHours(3L))
//						.serializeValuesWith(SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer())));
//	}

    /**
     * Subscriber登録
     *
     * @return
     * @throws MalformedURLException
     * @throws URISyntaxException
     */
//    @Bean
//    public RedisMessageListenerContainer redisContainer() throws URISyntaxException {
//        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
//        container.setConnectionFactory(redisConnectionFactory());
//        container.addMessageListener(bjTopicEventSubscriber, new ChannelTopic(BjCacheEnum.topicEvent.name()));
//        return container;
//    }

	/**
	 * 接続情報をRedisに設定する。<br>
	 * REDIS_URLで指定したURLがうまいこと設定されるらしい。
	 *
	 * @return
	 */
	@Bean
	LettuceClientConfigurationBuilderCustomizer lettuceClientConfigurationBuilderCustomizer() {
		return clientConfigurationBuilder -> {
			if (clientConfigurationBuilder.build().isUseSsl()) {
				clientConfigurationBuilder.useSsl().disablePeerVerification();
			}
		};
	}

    /**
     * 期限切れのTempViewを除去するイベントリスナー（らしい）
     *
     * @return
     */
//    @Bean
//    public ApplicationListener<RedisKeyExpiredEvent<TempView>> eventListener() {
//    	return event -> {
//    		log.info(String.format("Received expire event for key=%s with value %s.",
//    				new String(event.getSource()), event.getValue()));
//    	};
//    }

	/**
	 * Object用RedisTemplateをDIに登録
	 *
	 * @param lettuceConnectionFactory
	 * @return
	 */
	@Bean
	RedisTemplate<String, Object> objectRedisTemplate(LettuceConnectionFactory lettuceConnectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<String, Object>();
        redisTemplate.setConnectionFactory(lettuceConnectionFactory);
        //
        RedisSerializer<String> stringSerializer = new StringRedisSerializer();
        redisTemplate.setKeySerializer(stringSerializer);
        redisTemplate.setValueSerializer(stringSerializer);
        redisTemplate.setHashKeySerializer(stringSerializer);
        redisTemplate.setHashValueSerializer(stringSerializer);
        return redisTemplate;
	}

	/**
	 * Spring Data Redis用のRedisTemplate
	 *
	 * @param redisConnectionFactory
	 * @return
	 */
	@Bean
	RedisTemplate<?, ?> redisTemplate(LettuceConnectionFactory lettuceConnectionFactory) {

		RedisTemplate<byte[], byte[]> template = new RedisTemplate<byte[], byte[]>();
		template.setConnectionFactory(lettuceConnectionFactory);
		return template;
	}
}
