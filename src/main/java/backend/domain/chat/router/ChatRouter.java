package backend.domain.chat.router;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.*;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import backend.domain.chat.handler.ChatMessageHandler;
import backend.domain.chat.handler.ChatRoomHandler;

@Configuration
public class ChatRouter {

	@Bean
	public RouterFunction<ServerResponse> chatRoomRoute(ChatRoomHandler chatRoomHandler) {
		return route()
			.nest(path("/chat/room"), builder ->
				builder
					.POST("/create", chatRoomHandler::createChatRoom)
					.GET("/{roomId}/get", chatRoomHandler::getChatRoom)
					.PUT("/{roomId}/edit", chatRoomHandler::updateChatRoom)
					.DELETE("/{roomId}/delete", chatRoomHandler::deleteChatRoom)
					.GET("/list", chatRoomHandler::getAllChatRoom))
			.build();
	}

	@Bean
	public RouterFunction<ServerResponse> chatMessageRoute(ChatMessageHandler chatMessageHandler) {
		return route()
			.nest(path("/chat/message"), builder ->
				builder
					.GET("/{roomId}/get", chatMessageHandler::getChatMessages)
					.POST("/send", chatMessageHandler::sendChatMessage))
			.build();
	}
}
