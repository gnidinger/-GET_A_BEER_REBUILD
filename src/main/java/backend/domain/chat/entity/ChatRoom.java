package backend.domain.chat.entity;

import java.util.HashSet;
import java.util.Set;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@Builder
@ToString
@Document(collection = "chat_room")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoom {

	@Id
	private String id;
	private String name;
	private Set<String> userSet = new HashSet<>();

	public void addUser(String userId) {
		userSet.add(userId);
	}

	public void removeUser(String userId) {
		userSet.remove(userId);
	}

	public Boolean isEmpty() {
		return userSet.isEmpty();
	}

	public void update(ChatRoom chatRoom) {
		this.name = chatRoom.getName();
	}
}
