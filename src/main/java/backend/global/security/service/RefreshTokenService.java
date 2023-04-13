package backend.global.security.service;

import java.util.Arrays;
import java.util.stream.Stream;

import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;

import backend.domain.user.entity.User;
import backend.global.exception.BusinessLogicException;
import backend.global.exception.ExceptionCode;
import backend.global.redis.repository.ReactiveRedisTokenRepository;
import backend.global.security.cookieManager.CookieManager;
import backend.global.security.jwt.JwtTokenizer;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
	private final ReactiveRedisTokenRepository reactiveRedisTokenRepository;
	private final CookieManager cookieManager;
	private final JwtTokenizer jwtTokenizer;

	public Mono<ResponseEntity<Object>> regenerateToken(ServerWebExchange serverWebExchange, User user) {

		String email = user.getEmail();

		String[] cookies = serverWebExchange.getRequest().getHeaders().get("Cookie").toString().split(";");

		Stream<String> stream = Arrays.stream(cookies)
			.map(cookie -> cookie.replace(" ", ""))
			.filter(c -> c.startsWith('[' + "refreshToken"));

		String value = stream.reduce((first, second) -> second)
			.map(v -> v.replace('[' + "refreshToken=", ""))
			.orElse(null);

		return reactiveRedisTokenRepository.findByUid(email)
			.switchIfEmpty(Mono.error(new BusinessLogicException(ExceptionCode.TOKEN_NOT_FOUND)))
			.flatMap(token -> {
				if (!token.equals(value)) {
					return Mono.error(new BusinessLogicException(ExceptionCode.TOKEN_NOT_MATCHED));
				} else {
					return Mono.just(token);
				}
			})
			.flatMap(token -> {
				reactiveRedisTokenRepository.deleteByUid(email).subscribe();
				String accessToken = jwtTokenizer.delegateAccessToken(user);
				String refreshToken = jwtTokenizer.delegateRefreshToken(user);

				if (Boolean.TRUE.equals(reactiveRedisTokenRepository.hasKey(email))) {
					reactiveRedisTokenRepository.deleteByUid(user.getEmail()).subscribe();
				}

				ResponseCookie cookie = cookieManager.createCookie("refreshToken", refreshToken);

				return Mono.just(ResponseEntity.ok()
					.header("Authorization", "Bearer " + accessToken)
					.header("Set-Cookie", cookie.toString())
					.build());
			});

		// if (!Objects.equals(reactiveRedisTokenRepository.findByUid(email), value)) {
		// 	throw new BusinessLogicException(ExceptionCode.TOKEN_NOT_MATCHED);
		// }
		//
		// reactiveRedisTokenRepository.deleteByUid(email).subscribe();
		//
		// String accessToken = jwtTokenizer.delegateAccessToken(user);
		// String refreshToken = jwtTokenizer.delegateRefreshToken(user);
		//
		// if (Boolean.TRUE.equals(reactiveRedisTokenRepository.hasKey(email))) {
		// 	reactiveRedisTokenRepository.deleteByUid(user.getEmail()).subscribe();
		// }
		//
		// ResponseCookie cookie = cookieManager.createCookie("refreshToken", refreshToken);
		//
		// return Mono.just(ResponseEntity.ok()
		// 	.header("Authorization", "Bearer " + accessToken)
		// 	.header("Set-Cookie", cookie.toString())
		// 	.build());
	}
}
