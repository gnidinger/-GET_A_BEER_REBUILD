package backend.global.security.Handler;

import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import backend.domain.user.entity.User;
import backend.domain.user.repository.UserRepository;
import backend.domain.user.service.UserService;
import backend.global.exception.BusinessLogicException;
import backend.global.exception.ExceptionCode;
import backend.global.redis.repository.ReactiveRedisTokenRepository;
import backend.global.security.config.PBKDF2Encoder;
import backend.global.security.cookieManager.CookieManager;
import backend.global.security.dto.SigninDto;
import backend.global.security.jwt.JwtTokenizer;
import backend.global.security.service.GoogleService;
import backend.global.security.service.KakaoService;
import backend.global.security.service.NaverService;
import backend.global.security.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class SecurityHandler {

	private final JwtTokenizer jwtTokenizer;
	private final PBKDF2Encoder passwordEncoder;
	private final CookieManager cookieManager;
	private final UserService userService;
	private final UserRepository userRepository;
	private final RefreshTokenService refreshTokenService;
	private final ReactiveRedisTokenRepository reactiveRedisTokenRepository;
	private final KakaoService kakaoService;
	private final NaverService naverService;
	private final GoogleService googleService;

	public Mono<ServerResponse> signIn(ServerRequest serverRequest) {

		return serverRequest.bodyToMono(SigninDto.Request.class)
			.flatMap(request -> userRepository.findByEmail(request.getEmail())
				.switchIfEmpty(Mono.error(new BusinessLogicException(ExceptionCode.SIGNIN_FAILED)))
				.flatMap(user -> {
					if (passwordEncoder.matches(request.getPassword(), user.getPassword())) {
						return Mono.just(user);
					} else {
						return Mono.error(new BusinessLogicException(ExceptionCode.SIGNIN_FAILED));
					}
				})
			)
			.flatMap(user -> {
				String accessToken = jwtTokenizer.delegateAccessToken(user);
				String refreshToken = jwtTokenizer.delegateRefreshToken(user);
				reactiveRedisTokenRepository.saveRefreshToken(user.getEmail(), refreshToken).subscribe();

				ResponseCookie cookie = cookieManager.createCookie("refreshToken", refreshToken);

				SigninDto.Response response = SigninDto.Response.builder()
					.id(user.getId())
					.email(user.getEmail())
					.nickname(user.getNickname())
					.build();

				return ServerResponse.ok()
					.header("Authorization", "Bearer " + accessToken)
					.header("Set-Cookie", cookie.toString())
					.bodyValue(response);
			});
	}

	public Mono<ServerResponse> oauth(ServerRequest serverRequest, String providerId, String code) {

		Mono<User> userMono = null;

		switch (providerId) {

			case "kakao":
				userMono = kakaoService.doFilter(code);
				break;

			case "naver":
				userMono = naverService.doFilter(code);
				break;

			case "google":
				userMono = googleService.doFilter(code);
				break;
		}

		return userMono
			.flatMap(user -> {
				String accessToken = jwtTokenizer.delegateAccessToken(user);
				String refreshToken = jwtTokenizer.delegateRefreshToken(user);
				reactiveRedisTokenRepository.saveRefreshToken(user.getEmail(), refreshToken).subscribe();

				ResponseCookie cookie = cookieManager.createCookie("refreshToken", refreshToken);

				SigninDto.Response response = SigninDto.Response.builder()
					.id(user.getId())
					.email(user.getEmail())
					.nickname(user.getNickname())
					.build();

				return ServerResponse.ok()
					.header("Authorization", "Bearer " + accessToken)
					.header("Set-Cookie", cookie.toString())
					.bodyValue(response);
			});
	}

	public Mono<ServerResponse> refreshToken(ServerRequest serverRequest) {

		Mono<User> currentUserMono = userService.getCurrentUser();

		Mono<ResponseEntity<Object>> objectMono = currentUserMono
			.flatMap(user -> {
				String refreshToken = cookieManager.outCookie(serverRequest.exchange(), "refreshToken");
				if (!jwtTokenizer.validateToken(refreshToken)) {
					throw new BusinessLogicException(ExceptionCode.TOKEN_EXPIRED);
				}
				return refreshTokenService.regenerateToken(serverRequest.exchange(), user);
			});

		return objectMono
			.flatMap(responseEntity -> {
				return ServerResponse
					.ok()
					.headers(httpHeaders -> responseEntity.getHeaders().forEach(httpHeaders::addAll))
					.build();
			});
	}
}
