package backend.domain.beer.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import backend.domain.beer.entity.Beer;

public interface BeerRepository extends ReactiveMongoRepository<Beer, String> {
}
