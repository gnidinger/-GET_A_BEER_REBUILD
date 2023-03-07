package backend.global.redis.test;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.*;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import backend.domain.user.handler.UserHandler;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class RedisRouter {
	private final RedisHandler redisHandler;

	@Bean
	public RouterFunction<ServerResponse> redisRoute(UserHandler userHandler) {
		return route()
			.nest(path("/redis"), builder ->
				builder

					.GET("/reactive-list", serverRequest ->
						ServerResponse.ok().contentType(MediaType.TEXT_EVENT_STREAM)
							.body(redisHandler.findReactorList(), String.class))

					.GET("/normal-list", serverRequest ->
						ServerResponse.ok().contentType(MediaType.TEXT_EVENT_STREAM)
							.body(redisHandler.findNormalList(), String.class))

					.GET("/load", serverRequest -> {
						redisHandler.loadData();
						return ServerResponse.ok()
							.body(BodyInserters.fromValue("Load Data Completed"));
					}))
			.build();
	}
}
