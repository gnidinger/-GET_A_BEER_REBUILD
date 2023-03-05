package backend.global.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

import backend.global.security.manager.CustomAuthenticationManager;
import backend.global.security.repository.CustomSecurityContextRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@AllArgsConstructor
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class ReactiveSecurityConfig {

	private CustomAuthenticationManager customAuthenticationManager;
	private CustomSecurityContextRepository customSecurityContextRepository;

	@Bean
	public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity httpSecurity) {

		return httpSecurity
			.exceptionHandling()
			.authenticationEntryPoint(((exchange, ex) ->
				Mono.fromRunnable(() -> exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED))))
			.accessDeniedHandler((exchange, denied) ->
				Mono.fromRunnable(() -> exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN)))
			.and()
			.csrf().disable()
			.httpBasic().disable()
			.authenticationManager(customAuthenticationManager)
			.securityContextRepository(customSecurityContextRepository)
			.authorizeExchange()
			.pathMatchers(HttpMethod.OPTIONS).permitAll()
			.pathMatchers("/login").permitAll()
			.anyExchange().authenticated()
			.and()
			.build();
	}
}
