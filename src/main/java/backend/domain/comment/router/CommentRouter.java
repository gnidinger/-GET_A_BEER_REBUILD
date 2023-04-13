package backend.domain.comment.router;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.*;

import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import backend.domain.comment.entity.Comment;
import backend.domain.comment.handler.CommentHandler;
import backend.domain.pairing.entity.Pairing;
import backend.domain.rating.entity.Rating;
import reactor.core.publisher.Mono;

@Component
public class CommentRouter {

	@Bean
	public RouterFunction<ServerResponse> commentRoute(CommentHandler commentHandler) {
		return route()
			.nest(path("/comments"), builder ->
				builder
					// .GET("/{commentId}/get", commentHandler::readcomment)
					.GET("/{commentId}/get", commentHandler::readComment)
					.GET("/list", request -> commentHandler.readComments())
					.PUT("/{commentId}/patch", commentHandler::updateComment)
					.DELETE("/{commentId}/delete", commentHandler::deleteComment))
			.POST("/ratings/{ratingId}/comments/post", commentHandler::createRatingComment)
			.POST("/pairings/{pairingId}/comments/post", commentHandler::createPairingComment)
			.GET("/ratings/{ratingId}/comments/get", serverRequest -> {
				int page = Integer.parseInt(serverRequest.queryParam("page").orElse("1"));
				Mono<Page<Comment>> pageMono = commentHandler.readRatingCommentPageMono(serverRequest, page);
				return ServerResponse.ok().body(pageMono, new ParameterizedTypeReference<Page<Rating>>() {});
			})
			.GET("/pairings/{pairingId}/comments/get", serverRequest -> {
				int page = Integer.parseInt(serverRequest.queryParam("page").orElse("1"));
				Mono<Page<Comment>> pageMono = commentHandler.readPairingCommentPageMono(serverRequest, page);
				return ServerResponse.ok().body(pageMono, new ParameterizedTypeReference<Page<Rating>>() {});
			})
			.build();
	}
}
