package backend.global.security.manager;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import backend.global.security.jwt.JwtTokenizer;
import io.jsonwebtoken.Claims;
import lombok.AllArgsConstructor;
import reactor.core.publisher.Mono;

@Component
@AllArgsConstructor
public class CustomAuthenticationManager implements ReactiveAuthenticationManager {

	private JwtTokenizer jwtTokenizer;

	@Override
	public Mono<Authentication> authenticate(Authentication authentication) {

		String authToken = authentication.getCredentials().toString();
		String email = jwtTokenizer.getUsernameFromToken(authToken);

		return Mono.just(jwtTokenizer.validateToken(authToken))
			.filter(valid -> valid)
			.switchIfEmpty(Mono.empty())
			.map(valid -> {
				Claims claims = jwtTokenizer.getAllClaimsFromToken(authToken);
				List<String> rolesMap = claims.get("role", List.class);
				return new UsernamePasswordAuthenticationToken(
					email,
					null,
					rolesMap.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList())
				);
			});
	}
}
