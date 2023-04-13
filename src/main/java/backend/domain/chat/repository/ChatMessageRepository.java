package backend.domain.chat.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import backend.domain.chat.entity.ChatMessage;
import reactor.core.publisher.Flux;

public interface ChatMessageRepository extends ReactiveMongoRepository<ChatMessage, String> {

	Flux<ChatMessage> findByRoomIdOrderByTimestampDesc(String roomId);
}
