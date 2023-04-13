package backend.domain.beer.service;

import org.springframework.stereotype.Service;

import backend.domain.beer.entity.Beer;
import backend.domain.beer.exception.BeerNotFoundException;
import backend.domain.beer.repository.BeerMongoRepository;
import backend.domain.beer.repository.BeerRepository;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class BeerService {
	private final BeerRepository beerRepository;
	private final BeerMongoRepository beerMongoRepository;

	public Mono<Beer> findBeerDetailsByBeerId(String beerId) {
		// return beerRepository.findById(beerId)
		return beerMongoRepository.findById(beerId)
			.switchIfEmpty(Mono.error(new BeerNotFoundException()));
	}

	public Mono<Beer> findBeerByBeerId(String beerId) {
		return beerRepository.findById(beerId)
			.switchIfEmpty(Mono.error(new BeerNotFoundException()));
	}
}
