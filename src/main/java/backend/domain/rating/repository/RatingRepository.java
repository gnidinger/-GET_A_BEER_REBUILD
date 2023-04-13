package backend.domain.rating.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import backend.domain.rating.entity.Rating;
import reactor.core.publisher.Flux;

public interface RatingRepository extends ReactiveMongoRepository<Rating, String> {

	Flux<Rating> findByUserId(String userId);
}
