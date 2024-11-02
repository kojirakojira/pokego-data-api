package jp.brainjuice.pokego.config;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.data.redis.LettuceClientConfigurationBuilderCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
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

	private String envUrl;

	private static final String CONN_CREATED_MESSAGE_FORMAT = "Redis connection factory created. redis.env.url={0} "
			+ "(For more details, please check the application.yml specific to that environment.)";

	private static final String DEFAULT_MESSAGE_FORMAT = "Redis connection factory created. Because there was no URL defined, destination server is localhost:6379.";

	// redisバージョン6以降の仕様（らしい）
	private static final String DUMMY_USERNAME = "h";

	public RedisConfig(@Value("${redis.env.url}") String envUrl) {
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
	 * SSL/TLSの設定。
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

	@Bean
	LettuceConnectionFactory redisConnectionFactory() throws URISyntaxException {

		if (StringUtils.isEmpty(envUrl)) {
			// 存在しない場合はlocalhost:6379(LettuceConnectionFacotry上のデフォルト値)で設定する。
			log.info(DEFAULT_MESSAGE_FORMAT);
			return new LettuceConnectionFactory();
		}

		URI uri = new URI(envUrl);

		String host = uri.getHost();
		int port = uri.getPort();

		RedisStandaloneConfiguration conf = new RedisStandaloneConfiguration();
		conf.setHostName(host);
		conf.setPort(port);

		String userInfo = uri.getUserInfo();

		if (!StringUtils.isEmpty(userInfo)) {

			String[] userInfoArr = userInfo.split(":", 2);

			String username = userInfoArr[0];
			if (!StringUtils.isEmpty(username) && !DUMMY_USERNAME.equals(username)) {
				conf.setUsername(username);
			}

			String password = userInfoArr[1];
			conf.setPassword(password);
		}

		LettuceConnectionFactory factory = new LettuceConnectionFactory(conf);

		log.info(MessageFormat.format(CONN_CREATED_MESSAGE_FORMAT, envUrl));

		return factory;
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
