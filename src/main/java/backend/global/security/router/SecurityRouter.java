package backend.global.security.router;

import static org.springframework.web.reactive.function.server.RouterFunctions.*;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import backend.global.security.Handler.SecurityHandler;
import backend.global.security.dto.SigninDto;
import reactor.core.publisher.Mono;

@Configuration
public class SecurityRouter {

	@Bean
	public RouterFunction<ServerResponse> securityRoute(SecurityHandler securityHandler) {
		return route()
			.POST("/signin", securityHandler::signIn)
			.GET("/oauth/{providerId}?code={code}", serverRequest -> {
				String providerId = String.valueOf(serverRequest.pathVariable("providerId"));
				String code = String.valueOf(serverRequest.queryParam("code").orElse("new"));
				Mono<ServerResponse> userMono = securityHandler.oauth(serverRequest, providerId, code);
				return ServerResponse.status(HttpStatus.CREATED).body(userMono, SigninDto.Response.class);
			})
			.POST("/refresh", securityHandler::refreshToken)
			.build();
	}
}
