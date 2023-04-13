package backend.domain.chat.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import backend.domain.chat.entity.ChatRoom;

public interface ChatRoomRepository extends ReactiveMongoRepository<ChatRoom, String> {
}
