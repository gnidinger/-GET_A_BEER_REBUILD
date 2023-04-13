package backend.domain.rating.router;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.*;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import backend.domain.rating.entity.Rating;
import backend.domain.rating.handler.RatingHandler;
import reactor.core.publisher.Mono;

@Configuration
public class RatingRouter {

	@Bean
	public RouterFunction<ServerResponse> ratingRoute(RatingHandler ratingHandler) {
		return route()
			.nest(path("/ratings"), builder ->
				builder
					.GET("/{ratingId}/get", ratingHandler::readRating)
					.GET("/list", request -> ratingHandler.readRatings())
					.PUT("/{ratingId}/edit", ratingHandler::updateRating)
					.DELETE("/{ratingId}/delete", ratingHandler::deleteRating))
			.POST("/beers/{beerId}/ratings/post", ratingHandler::createRating)
			.GET("/beers/{beerId}/ratings/get", serverRequest -> {
				int page = Integer.parseInt(serverRequest.queryParam("page").orElse("1"));
				String sort = String.valueOf(serverRequest.queryParam("sort").orElse("new"));
				// int size = Integer.parseInt(request.queryParam("size").orElse("10"));
				Mono<Page<Rating>> pageMono = ratingHandler.readRatingPageMono(serverRequest, sort, page);
				return ServerResponse.ok().body(pageMono, new ParameterizedTypeReference<Page<Rating>>() {
				});
			})
			.build();
	}
}
