package backend.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;

import backend.domain.chat.entity.ChatMessage;
import backend.domain.chat.entity.ChatRoom;

@Configuration
public class ReactiveRedisConfig {

	@Value("${spring.redis.host}")
	private String redisHost;

	@Value("${spring.redis.port}")
	private int redisPort;

	@Bean
	@Primary
	public ReactiveRedisConnectionFactory reactiveRedisConnectionFactory() {
		return new LettuceConnectionFactory(redisHost, redisPort);
	}

	@Bean
	public ReactiveRedisTemplate<String, Object> reactiveRedisTemplate(
		ReactiveRedisConnectionFactory connectionFactory) {
		Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(Object.class);
		ObjectMapper mapper = new ObjectMapper();
		mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
		mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
		serializer.setObjectMapper(mapper);

		RedisSerializationContext<String, Object> serializationContext = RedisSerializationContext
			.<String, Object>newSerializationContext(RedisSerializer.string())
			.value(serializer)
			.build();

		return new ReactiveRedisTemplate<>(connectionFactory, serializationContext);
	}

	@Bean
	public ReactiveRedisTemplate<String, ChatMessage> chatMessageReactiveRedisTemplate(
		ReactiveRedisConnectionFactory connectionFactory) {
		Jackson2JsonRedisSerializer<ChatMessage> serializer = new Jackson2JsonRedisSerializer<>(ChatMessage.class);
		ObjectMapper mapper = new ObjectMapper();
		mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
		mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
		serializer.setObjectMapper(mapper);

		RedisSerializationContext<String, ChatMessage> serializationContext = RedisSerializationContext
			.<String, ChatMessage>newSerializationContext(RedisSerializer.string())
			.value(serializer)
			.build();

		return new ReactiveRedisTemplate<>(connectionFactory, serializationContext);
	}

	@Bean
	public ReactiveRedisTemplate<String, ChatRoom> chatRoomReactiveRedisTemplate(
		ReactiveRedisConnectionFactory connectionFactory) {
		Jackson2JsonRedisSerializer<ChatRoom> serializer = new Jackson2JsonRedisSerializer<>(ChatRoom.class);
		ObjectMapper mapper = new ObjectMapper();
		mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
		mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
		serializer.setObjectMapper(mapper);

		RedisSerializationContext<String, ChatRoom> serializationContext = RedisSerializationContext
			.<String, ChatRoom>newSerializationContext(RedisSerializer.string())
			.value(serializer)
			.build();

		return new ReactiveRedisTemplate<>(connectionFactory, serializationContext);
	}

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
