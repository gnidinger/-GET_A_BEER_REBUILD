package backend.domain.beer.router;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.*;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import backend.domain.beer.handler.BeerHandler;

@Configuration
public class BeerRouter {

	@Bean
	public RouterFunction<ServerResponse> beerRoute(BeerHandler beerHandler) {
		return route()
			.nest(path("/beers"), builder ->
				builder
					.POST("/post", beerHandler::createBeer)
					// .GET("/{beerId}/get", beerHandler::readBeer)
					.GET("/{beerId}/get", beerHandler::readBeer)
					.GET("/list", request -> beerHandler.readBeers())
					.PATCH("/{beerId}/patch", beerHandler::updateBeer)
					.DELETE("/{beerId}/delete", beerHandler::deleteBeer))
			.GET("/v1/helloworld", (request -> ServerResponse.ok().bodyValue("HelloWorld")))
			.GET("/v1/greeting/{name}",
				(request -> ServerResponse.ok().bodyValue("hello " + request.pathVariable("name"))))
			.build();
	}
}
