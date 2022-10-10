package jp.brainjuice.pokego.cache;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair;

import jp.brainjuice.pokego.filter.log.LogUtils;
import jp.brainjuice.pokego.utils.BjConfigEnum;

@Configuration
@EnableCaching
@EnableRedisRepositories
public class CacheConfig {

	@Value("${redis.env.url}")
	private String envUrl;

//	private BjTopicEventSubscriber bjTopicEventSubscriber;
//
	private static final String CONNECTED_MESSAGE_FORMAT = "Redis is connected in {0} mode. {1}={2}";
//
//	@Autowired
//	public CacheConfig(BjTopicEventSubscriber bjTopicEventSubscriber) {
//		this.bjTopicEventSubscriber = bjTopicEventSubscriber;
//	}

	/**
	 * デフォルトキャッシュ設定
	 *
	 * @return
	 */
	@Bean
	public RedisCacheConfiguration cacheConfiguration() {
		return RedisCacheConfiguration
				.defaultCacheConfig()
				.entryTtl(Duration.ofHours(5L)).disableCachingNullValues()
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

    @Bean
    public RedisConnectionFactory redisConnectionFactory() throws URISyntaxException {
		String envRedisUrl = System.getenv(envUrl);
		URI uri = new URI(envRedisUrl);

		String host = uri.getHost();
		int port = uri.getPort();
		String password = uri.getUserInfo().split(":", 2)[1];

		LettuceConnectionFactory factory;
		String env = System.getenv(BjConfigEnum.System.SPRING_PROFILES_ACTIVE.name());
		// 本番環境でのみクラスターモード
		if ("production".equals(env)) {
			LogUtils.getLog(this).info(MessageFormat.format(CONNECTED_MESSAGE_FORMAT, "cluster", envUrl, envRedisUrl));
			RedisClusterConfiguration clusterConfiguration = new RedisClusterConfiguration();
			clusterConfiguration.clusterNode(host, port);
			clusterConfiguration.setPassword(password);
			factory = new LettuceConnectionFactory(clusterConfiguration);
		} else {
			LogUtils.getLog(this).info(MessageFormat.format(CONNECTED_MESSAGE_FORMAT, "normal", envUrl, envRedisUrl));
			RedisStandaloneConfiguration conf = new RedisStandaloneConfiguration();
			conf.setHostName(host);
			conf.setPort(port);
			conf.setPassword(password);
			factory = new LettuceConnectionFactory(conf);
		}

		return factory;
    }

    /**
     * Integer用RedisTemplateをDIに登録
     *
     * @param lettuceConnectionFactory
     * @return
     */
	@Bean
	public RedisTemplate<String, Integer> integerRedisTemplate(LettuceConnectionFactory lettuceConnectionFactory) {
        RedisTemplate<String, Integer> redisTemplate = new RedisTemplate<String, Integer>();
        redisTemplate.setConnectionFactory(lettuceConnectionFactory);
        return redisTemplate;
	}

	/**
	 * Object用RedisTemplateをDIに登録
	 *
	 * @param lettuceConnectionFactory
	 * @return
	 */
	@Bean
	public RedisTemplate<String, Object> objectRedisTemplate(LettuceConnectionFactory lettuceConnectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<String, Object>();
        redisTemplate.setConnectionFactory(lettuceConnectionFactory);
        return redisTemplate;
	}

	/**
	 * Spring Data Redis用のRedisTemplate
	 *
	 * @param redisConnectionFactory
	 * @return
	 */
	@Bean
	public RedisTemplate<?, ?> redisTemplate(RedisConnectionFactory redisConnectionFactory) {

		RedisTemplate<byte[], byte[]> template = new RedisTemplate<byte[], byte[]>();
		template.setConnectionFactory(redisConnectionFactory);
		return template;
	}
}
