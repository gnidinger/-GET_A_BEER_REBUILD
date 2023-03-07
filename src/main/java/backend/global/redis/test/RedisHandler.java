package backend.global.redis.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

@Component
@RequiredArgsConstructor
public class RedisHandler {
	private final ReactiveRedisConnectionFactory reactiveRedisConnectionFactory;
	private final ReactiveRedisOperations<String, String> reactiveRedisOperations;
	private final RedisTemplate<String, String> stringStringRedisTemplate;
	private static final AtomicInteger count = new AtomicInteger(0);

	public void loadData() {
		List<String> data = new ArrayList<>();
		IntStream.range(0, 100000).forEach(i -> data.add(UUID.randomUUID().toString()));

		Flux<String> stringFlux = Flux.fromIterable(data);

		reactiveRedisConnectionFactory.getReactiveConnection()
			.serverCommands()
			.flushAll()
			.thenMany(stringFlux.flatMap(uid -> reactiveRedisOperations.opsForValue()
				.set(String.valueOf(count.getAndAdd(1)), uid)))
			.subscribe();
	}

	public Flux<String> findReactorList() {
		return reactiveRedisOperations
			.keys("*")
			.flatMap(key -> reactiveRedisOperations.opsForValue().get(key));
	}

	public Flux<String> findNormalList() {
		return Flux.fromIterable(Objects.requireNonNull(stringStringRedisTemplate.keys("*"))
			.stream()
			.map(key -> stringStringRedisTemplate.opsForValue().get(key))
			.collect(Collectors.toList()));
	}
}
