package jp.brainjuice.pokego.cache;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import lombok.extern.slf4j.Slf4j;

@Configuration
@EnableCaching
//@EnableRedisRepositories(enableKeyspaceEvents = EnableKeyspaceEvents.ON_STARTUP)
@Slf4j
public class RedisConfig {

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

    @Bean
    public LettuceConnectionFactory redisConnectionFactory() throws URISyntaxException {
		String envRedisUrl = System.getenv(envUrl);
		URI uri = new URI(envRedisUrl);

		String host = uri.getHost();
		int port = uri.getPort();
		String password = uri.getUserInfo().split(":", 2)[1];

		LettuceConnectionFactory factory;
//		String env = System.getenv(BjConfigEnum.System.SPRING_PROFILES_ACTIVE.name());
		// 本番環境でのみクラスターモード
//		if ("production".equals(env)) {
//			RedisClusterConfiguration clusterConfiguration = new RedisClusterConfiguration();
//			clusterConfiguration.clusterNode(host, port);
//			clusterConfiguration.setPassword(password);
//			factory = new LettuceConnectionFactory(clusterConfiguration);
//			log.info(MessageFormat.format(CONNECTED_MESSAGE_FORMAT, "cluster", envUrl, envRedisUrl));
//		} else {
		RedisStandaloneConfiguration conf = new RedisStandaloneConfiguration();
		conf.setHostName(host);
		conf.setPort(port);
		conf.setPassword(password);
		factory = new LettuceConnectionFactory(conf);
		log.info(MessageFormat.format(CONNECTED_MESSAGE_FORMAT, "normal", envUrl, envRedisUrl));
//		}

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
	public RedisTemplate<String, Object> objectRedisTemplate(LettuceConnectionFactory lettuceConnectionFactory) {
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
	public RedisTemplate<?, ?> redisTemplate(RedisConnectionFactory redisConnectionFactory) {

		RedisTemplate<byte[], byte[]> template = new RedisTemplate<byte[], byte[]>();
		template.setConnectionFactory(redisConnectionFactory);
		return template;
	}
}
