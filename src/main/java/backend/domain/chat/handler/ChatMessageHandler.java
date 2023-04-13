package backend.domain.chat.handler;

import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import backend.domain.chat.entity.ChatMessage;
import backend.domain.chat.entity.ChatRoom;
import backend.domain.chat.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class ChatMessageHandler {

	private final ReactiveRedisTemplate<String, ChatMessage> stringChatMessageReactiveRedisTemplate;
	private final ReactiveRedisTemplate<String, ChatRoom> stringChatRoomReactiveRedisTemplate;
	private final ChatMessageRepository chatMessageRepository;

	public Mono<ServerResponse> getChatMessages(ServerRequest serverRequest) {

		String roomId = serverRequest.pathVariable("roomId");

		Flux<ChatMessage> chatMessageFlux = chatMessageRepository.findByRoomIdOrderByTimestampDesc(roomId);

		return ServerResponse.ok().body(chatMessageFlux, ChatMessage.class);
	}

	public Mono<ServerResponse> sendChatMessage(ServerRequest serverRequest) {

		Mono<ChatMessage> chatMessageMono = serverRequest.bodyToMono(ChatMessage.class);

		return chatMessageMono
			.flatMap(chatMessage -> {
				chatMessageRepository.save(chatMessage)
					.subscribe(savedChatMessage -> stringChatMessageReactiveRedisTemplate.opsForList()
						.leftPush(chatMessage.getRoomId(), savedChatMessage)
						.subscribe());
				return ServerResponse.ok().build();
			});
	}

}
