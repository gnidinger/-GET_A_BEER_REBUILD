package backend.global.security.controller;

import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebExchange;

import backend.domain.user.repository.UserRepository;
import backend.global.exception.BusinessLogicException;
import backend.global.exception.ExceptionCode;
import backend.global.redis.repository.ReactiveRedisTokenRepository;
import backend.global.security.config.PBKDF2Encoder;
import backend.global.security.cookieManager.CookieManager;
import backend.global.security.dto.SigninDto;
import backend.global.security.jwt.JwtTokenizer;
import backend.global.security.repository.CustomSecurityContextRepository;
import backend.global.security.service.RefreshTokenService;
import lombok.AllArgsConstructor;
import reactor.core.publisher.Mono;

@RestController
@AllArgsConstructor
public class SigninController {

	private JwtTokenizer jwtTokenizer;
	private PBKDF2Encoder passwordEncoder;
	private final CookieManager cookieManager;
	private final UserRepository userRepository;
	private final RefreshTokenService refreshTokenService;
	private final CustomSecurityContextRepository customSecurityContextRepository;
	private final ReactiveRedisTokenRepository reactiveRedisTokenRepository;

	@PostMapping("/signin")
	public Mono<ResponseEntity<SigninDto.Response>> signin(@RequestBody SigninDto.Request request) {
		return userRepository.findByEmail(request.getEmail())
			.switchIfEmpty(Mono.error(new BusinessLogicException(ExceptionCode.SIGNIN_FAILED)))
			.flatMap(user -> {
				if (passwordEncoder.matches(request.getPassword(), user.getPassword())) {
					return Mono.just(user);
				} else {
					return Mono.error(new BusinessLogicException(ExceptionCode.SIGNIN_FAILED));
				}
			})
			.flatMap(user -> {
				String accessToken = jwtTokenizer.delegateAccessToken(user);
				String refreshToken = jwtTokenizer.delegateRefreshToken(user);
				reactiveRedisTokenRepository.saveRefreshToken(user.getEmail(), refreshToken).subscribe();

				ResponseCookie cookie = cookieManager.createCookie("refreshToken", refreshToken);

				return Mono.just(ResponseEntity.ok()
					.header("Authorization", "Bearer " + accessToken)
					.header("Set-Cookie", cookie.toString())
					.body(SigninDto.Response.builder()
						.id(user.getId())
						.email(user.getEmail())
						.nickname(user.getNickname())
						.build()));
			});
	}

	@PostMapping("/refresh")
	public Mono<ResponseEntity<Object>> test(ServerWebExchange serverWebExchange) {

		return customSecurityContextRepository.load(serverWebExchange)
			.switchIfEmpty(Mono.error(new BusinessLogicException(ExceptionCode.USER_NOT_FOUND)))
			.map(securityContext -> securityContext.getAuthentication().getName())
			.flatMap(userRepository::findByEmail)
			.flatMap(user -> {
				String refreshToken = cookieManager.outCookie(serverWebExchange, "refreshToken");
				if (!jwtTokenizer.validateToken(refreshToken)) {
					throw new BusinessLogicException(ExceptionCode.TOKEN_EXPIRED);
				}
				return refreshTokenService.regenerateToken(serverWebExchange, user);
			});

		// customSecurityContextRepository.load(serverWebExchange)
		// 	.map(securityContext -> securityContext.getAuthentication().getName())
		// 	.doOnNext(System.out::println)
		// 	.subscribe();
		//
		// String refreshToken = cookieManager.outCookie(serverWebExchange, "refreshToken");
		//
		// if (!jwtTokenizer.validateToken(refreshToken)) {
		// 	throw new BusinessLogicException(ExceptionCode.TOKEN_EXPIRED);
		// }
		//
		// System.out.println(refreshToken);
		//
		// return "test";
	}
}
