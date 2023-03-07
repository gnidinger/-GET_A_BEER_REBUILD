package backend.domain.user.router;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.*;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import backend.domain.user.handler.UserHandler;

@Configuration
public class UserRouter {

	@Bean
	public RouterFunction<ServerResponse> userRoute(UserHandler userHandler) {
		return route()
			.nest(path("/users"), builder ->
				builder
					.POST("/signup", userHandler::signup)
					// .POST("/signin", userHandler::signin)
					.PUT("/signin/first/{userId}", userHandler::firstSignin)
					.GET("/{userId}/get", userHandler::readUser)
					.GET("/list", request -> userHandler.readUsers())
					.PUT("/{userId}/patch", userHandler::updateUser)
					.DELETE("/{userId}/delete", userHandler::deleteUser))
			.build();
	}
}
