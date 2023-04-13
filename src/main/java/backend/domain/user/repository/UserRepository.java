package backend.domain.user.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import backend.domain.user.entity.User;
import reactor.core.publisher.Mono;

public interface UserRepository extends ReactiveMongoRepository<User, String> {

	Mono<User> findByNickname(String nickname);
	Mono<User> findByEmail(String email);
	Mono<User> findByProviderId(String providerId);
}
