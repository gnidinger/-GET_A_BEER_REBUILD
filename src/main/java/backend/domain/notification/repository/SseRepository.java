package backend.domain.notification.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import backend.domain.notification.entity.Notification;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

@Component
public class SseRepository {

	private final Map<String, Sinks.Many<ServerSentEvent>> emitters = new ConcurrentHashMap<>();
	private final Map<String, Object> eventCache = new ConcurrentHashMap<>();

	public void save(String emitterId, Sinks.Many<ServerSentEvent> emitter) {

		// 완료 및 예외처리
		Disposable disposable = emitter
			.asFlux()
			.doOnError(e -> deleteAllStartByWithId(emitterId))
			.doOnCancel(() -> deleteAllStartByWithId(emitterId))
			.doFinally(signalType -> deleteAllStartByWithId(emitterId))
			.subscribe();

		emitters.put(emitterId, emitter);
		eventCache.put(emitterId, disposable);
	}

	public void saveEventCache(String id, Object event) {
		eventCache.put(id, event);
	}

	public Flux<Notification> findAllCachedNotifications(String userId) {
		Sinks.Many<ServerSentEvent> emitter = emitters.get(userId);
		if (emitter != null) {
			return emitter.asFlux()
				.filter(serverSentEvent -> serverSentEvent.event().equals("notification"))
				.map(serverSentEvent -> serverSentEvent.data().toString())
				.map(jsonString -> {
					try {
						return new ObjectMapper().readValue(jsonString, Notification.class);
					} catch (JsonProcessingException e) {
						throw new RuntimeException("Failed to deserialize Notification object");
					}
				});
		}
		return Flux.empty();
	}

	public void deleteAllStartByWithId(String id) {
		List<String> keysToRemove = new ArrayList<>();
		for (String key : emitters.keySet()) {
			if (key.startsWith(id)) {
				keysToRemove.add(key);
			}
		}
		for (String key : keysToRemove) {
			emitters.remove(key);
		}
	}
}
