package backend.domain.like.router;

import static org.springframework.web.reactive.function.server.RouterFunctions.*;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import backend.domain.like.handler.LikeHandler;

@Configuration
public class LikeRouter {

	@Bean
	public RouterFunction<ServerResponse> likeRoute(LikeHandler likeHandler) {
		return route()
			.PUT("/ratings/{ratingId}/like", likeHandler::ratingLike)
			.PUT("/pairings/{pairingId}/like", likeHandler::pairingLike)
			.PUT("/comments/{commentId}/like", likeHandler::commentLike)
			.build();
	}
}
