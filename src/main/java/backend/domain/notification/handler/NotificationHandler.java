package backend.domain.notification.handler;

import java.time.Duration;
import java.time.Instant;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import backend.domain.notification.entity.Notification;
import backend.domain.notification.repository.SseRepository;
import backend.domain.user.entity.User;
import backend.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.util.retry.Retry;

@Component
@RequiredArgsConstructor
public class NotificationHandler {

	private final UserService userService;
	private final SseRepository sseRepository;
	private final ReactiveMongoOperations reactiveMongoOperations;

	public Mono<ServerResponse> connect(ServerRequest serverRequest) {

		Mono<User> currentUserMono = userService.getCurrentUser();

		return currentUserMono
			.flatMap(user -> {
				String lastEventId = serverRequest.headers().firstHeader("Last-Event-ID");
				String emitterId = user.getId() + "_" + Instant.now().toEpochMilli();

				// Emitter 생성
				Sinks.Many<ServerSentEvent> emitter = Sinks.many().unicast().onBackpressureBuffer();

				sseRepository.save(emitterId, emitter);

				// 503 에러 방지를 위해 연결 완료 메시지 전송
				ServerSentEvent welcomeEvent = ServerSentEvent.builder()
					.id(emitterId)
					.event("notification")
					.data("SSE 연결 완료." + user.getNickname() + "님, 환영합니다.")
					.build();

				emitter.tryEmitNext(welcomeEvent);

				// 미수신 Event 목록 존재 시 전송, Event 유실 방지
				Flux<ServerSentEvent> cachedEventFlux = sseRepository.findAllCachedNotifications(user.getId())
					.map(notification -> ServerSentEvent.builder()
						.id(notification.getId().toString())
						.event("notification")
						.data(notification)
						.build())
					.map(event -> {
						emitter.tryEmitNext(event);
						return event;
					});

				// 새로운 알림 목록 존재시 전송.
				Query query = Query.query(Criteria.where("receiverId").is(user.getId())
					.and("_id").gt(new ObjectId(lastEventId)));

				Flux<ServerSentEvent> newEventFlux = reactiveMongoOperations.tail(query, Notification.class)
					.map(notification -> {
						ServerSentEvent serverSentEvent = ServerSentEvent.builder()
							.id(notification.getId().toString())
							.event("notification")
							.data(notification)
							.build();

						emitter.tryEmitNext(serverSentEvent);
						sseRepository.saveEventCache(user.getId(), notification);
						return serverSentEvent;
					})
					.retryWhen(Retry.backoff(Long.MAX_VALUE, Duration.ofSeconds(1)).maxBackoff(Duration.ofMinutes(1)));

				Flux<ServerSentEvent> serverSentEventFlux = Flux.concat(cachedEventFlux, newEventFlux);

				return ServerResponse.ok()
					.contentType(MediaType.TEXT_EVENT_STREAM)
					.body(serverSentEventFlux, ServerSentEvent.class);

			}).switchIfEmpty(ServerResponse.status(HttpStatus.UNAUTHORIZED).build());
	}
}

