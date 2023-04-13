package backend.domain.like.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import backend.domain.like.entity.Like;
import reactor.core.publisher.Mono;

public interface LikeRepository extends ReactiveMongoRepository<Like, String> {

	Mono<Like> findByUserIdAndRatingId(String userId, String ratingId);
	Mono<Like> findByUserIdAndPairingId(String userId, String pairingId);
	Mono<Like> findByUserIdAndCommentId(String userId, String commentId);
}
