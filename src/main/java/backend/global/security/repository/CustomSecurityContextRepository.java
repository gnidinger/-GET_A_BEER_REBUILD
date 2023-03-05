package backend.global.security.repository;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import backend.global.security.manager.CustomAuthenticationManager;
import reactor.core.publisher.Mono;

@Component
public class CustomSecurityContextRepository implements ServerSecurityContextRepository {

	private CustomAuthenticationManager customAuthenticationManager;

	public CustomSecurityContextRepository(CustomAuthenticationManager customAuthenticationManager) {
		this.customAuthenticationManager = customAuthenticationManager;
	}

	@Override
	public Mono<Void> save(ServerWebExchange exchange, SecurityContext context) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Mono<SecurityContext> load(ServerWebExchange exchange) {

		return Mono.justOrEmpty(exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION))
			.filter(authHeader -> authHeader.startsWith("Bearer"))
			.flatMap(authHeader -> {
				String authToken = authHeader.substring(7);
				Authentication authentication = new UsernamePasswordAuthenticationToken(authToken, authToken);
				return this.customAuthenticationManager.authenticate(authentication).map(SecurityContextImpl::new);
			});
	}
}
