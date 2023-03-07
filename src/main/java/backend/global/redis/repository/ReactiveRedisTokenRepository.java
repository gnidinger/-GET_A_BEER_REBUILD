package backend.global.redis.repository;

import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;

import backend.global.exception.BusinessLogicException;
import backend.global.exception.ExceptionCode;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Repository
@RequiredArgsConstructor
public class ReactiveRedisTokenRepository {
	private final ReactiveRedisTemplate<String, String> reactiveRedisTemplate;
	private static final String PREFIX = "REFRESH_TOKEN_UID:";

	public Mono<String> findByUid(String uid) {
		return reactiveRedisTemplate.opsForValue().get(PREFIX + uid);
	}

	public Mono<Boolean> hasKey(String uid) {
		return reactiveRedisTemplate.hasKey(PREFIX + uid);
	}
	public Mono<Long> deleteByUid(String uid) {
		return reactiveRedisTemplate.delete(PREFIX + uid);
	}

	public Mono<String> saveRefreshToken(String uid, String refreshToken) {
		return reactiveRedisTemplate.opsForValue().set(PREFIX + uid, refreshToken)
			.flatMap(success -> {
				if (success) {
					return Mono.just(refreshToken);
				} else {
					return Mono.error(new BusinessLogicException(ExceptionCode.SIGNIN_FAILED));
				}
			});
	}
}
