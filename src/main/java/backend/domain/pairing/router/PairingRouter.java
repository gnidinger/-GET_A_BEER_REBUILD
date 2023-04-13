package backend.domain.pairing.router;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.*;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import backend.domain.pairing.entity.Pairing;
import backend.domain.pairing.handler.PairingHandler;
import backend.domain.rating.entity.Rating;
import reactor.core.publisher.Mono;

@Configuration
public class PairingRouter {

	@Bean
	public RouterFunction<ServerResponse> pairingRoute(PairingHandler pairingHandler) {
		return route()
			.nest(path("/pairings"), builder ->
				builder
					.GET("/{pairingId}/get", pairingHandler::readPairing)
					.GET("/list", request -> pairingHandler.readPairings())
					.PUT("/{pairingId}/edit", pairingHandler::updatePairing)
					.DELETE("/{pairingId}/delete", pairingHandler::deletePairing))
			.POST("/beers/{beerId}/pairings/post", pairingHandler::createPairing)
			.GET("/beers/{beerId}/pairings/get", serverRequest -> {
				int page = Integer.parseInt(serverRequest.queryParam("page").orElse("1"));
				String category = String.valueOf(serverRequest.queryParam("category"));
				String sort = String.valueOf(serverRequest.queryParam("sort"));
				Mono<Page<Pairing>> pageMono = pairingHandler.readPairingPageMono(serverRequest, category, sort, page);
				return ServerResponse.ok().body(pageMono, new ParameterizedTypeReference<Page<Rating>>() {});
			})
			.build();
	}
}
