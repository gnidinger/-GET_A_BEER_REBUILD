package backend.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class ReactiveRedisConfig {

	@Value("${spring.redis.host}")
	private String redisHost;

	@Value("${spring.redis.port}")
	private int redisPort;

	@Bean
	public ReactiveRedisConnectionFactory reactiveRedisConnectionFactory() {
		return new LettuceConnectionFactory(redisHost, redisPort);
	}

	// @Bean
	// @Primary
	// ReactiveRedisOperations<String, String> redisOperations(ReactiveRedisConnectionFactory factory) {
	//
	// 	RedisSerializer<String> serializer = new StringRedisSerializer();
	//
	// 	Jackson2JsonRedisSerializer jackson2JsonRedisSerializer =
	// 		new Jackson2JsonRedisSerializer<>(String.class);
	//
	// 	RedisSerializationContext<String, String> serializationContext = RedisSerializationContext
	// 		.<String, String>newSerializationContext()
	// 		.key(serializer)
	// 		.value(serializer)
	// 		.hashKey(serializer)
	// 		.hashValue(serializer)
	// 		.build();
	// 	return new ReactiveRedisTemplate<>(factory, serializationContext);
	// }

	// @Bean
	// public ReactiveRedisTemplate<String, String> reactiveRedisTemplate(
	// 	ReactiveRedisConnectionFactory connectionFactory) {
	//
	// 	RedisSerializer<String> serializer = new StringRedisSerializer();
	//
	// 	Jackson2JsonRedisSerializer jackson2JsonRedisSerializer =
	// 		new Jackson2JsonRedisSerializer<>(String.class);
	//
	// 	RedisSerializationContext<String, String> serializationContext = RedisSerializationContext
	// 		.<String, String>newSerializationContext()
	// 		.key(serializer)
	// 		.value(serializer)
	// 		.hashKey(serializer)
	// 		.hashValue(serializer)
	// 		.build();
	//
	// 	return new ReactiveRedisTemplate<>(connectionFactory, serializationContext);
	// }
}
