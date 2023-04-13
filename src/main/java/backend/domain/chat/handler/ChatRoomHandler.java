package backend.domain.chat.handler;

import java.util.HashSet;
import java.util.UUID;

import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import backend.domain.chat.entity.ChatRoom;
import backend.domain.chat.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class ChatRoomHandler {

	private final ReactiveRedisTemplate<String, ChatRoom> stringChatRoomReactiveRedisTemplate;
	private final ChatRoomRepository chatRoomRepository;

	public Mono<ServerResponse> createChatRoom(ServerRequest serverRequest) {

		Mono<ChatRoom> chatRoomMono = serverRequest.bodyToMono(ChatRoom.class)
			.map(chatRoom -> ChatRoom.builder()
				.id(UUID.randomUUID().toString())
				.name(chatRoom.getName())
				.userSet(new HashSet<>())
				.build());

		return chatRoomMono
			.flatMap(chatRoom -> {
				chatRoomRepository.insert(chatRoom);
				return stringChatRoomReactiveRedisTemplate.opsForValue().set(chatRoom.getId(), chatRoom);
			})
			.flatMap(aBoolean -> ServerResponse.ok().bodyValue(aBoolean))
			.switchIfEmpty(ServerResponse.badRequest().build());
	}

	public Mono<ServerResponse> getChatRoom(ServerRequest serverRequest) {

		String roomId = serverRequest.pathVariable("roomId");

		return stringChatRoomReactiveRedisTemplate.opsForValue().get(roomId)
			.flatMap(chatRoom -> ServerResponse.ok().bodyValue(chatRoom))
			.switchIfEmpty(
				chatRoomRepository.findById(roomId)
					.flatMap(chatRoom -> stringChatRoomReactiveRedisTemplate.opsForValue()
						.set(chatRoom.getId(), chatRoom)
						.thenReturn(chatRoom)
						.flatMap(chatRoom1 -> ServerResponse.ok().bodyValue(chatRoom1)))
					.switchIfEmpty(ServerResponse.notFound().build()));
	}

	public Mono<ServerResponse> deleteChatRoom(ServerRequest serverRequest) {

		String roomId = serverRequest.pathVariable("roomId");

		return chatRoomRepository.deleteById(roomId)
			.flatMap(unused -> ServerResponse.noContent().build())
			.switchIfEmpty(ServerResponse.notFound().build());
	}

	public Mono<ServerResponse> updateChatRoom(ServerRequest serverRequest) {

		String roomId = serverRequest.pathVariable("roomId");

		Mono<ChatRoom> currentChatRoom = chatRoomRepository.findById(roomId);
		Mono<ChatRoom> updatedChatRoom = serverRequest.bodyToMono(ChatRoom.class);

		return currentChatRoom.zipWith(updatedChatRoom, (current, updated) -> {
				current.update(updated);
				chatRoomRepository.save(current);
				stringChatRoomReactiveRedisTemplate.opsForValue().set(current.getId(), current)
					.subscribe();
				return current;
			})
			.flatMap(chatRoom -> ServerResponse.ok().bodyValue(chatRoom))
			.switchIfEmpty(ServerResponse.notFound().build());
	}

	public Mono<ServerResponse> getAllChatRoom(ServerRequest serverRequest) {

		Flux<ChatRoom> chatRoomFlux = stringChatRoomReactiveRedisTemplate.keys("*")
			.flatMap(stringChatRoomReactiveRedisTemplate.opsForValue()::get);

		return ServerResponse.ok().body(chatRoomFlux, ChatRoom.class);
	}
}
