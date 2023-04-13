package backend.domain.rating.service;

import java.security.Principal;
import java.util.List;

import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Service;

import backend.domain.beer.exception.BeerNotFoundException;
import backend.domain.beer.repository.BeerRepository;
import backend.domain.rating.entity.Rating;
import backend.domain.rating.exception.RatingNotFoundException;
import backend.domain.rating.repository.RatingMongoRepository;
import backend.domain.rating.repository.RatingRepository;

import backend.domain.user.entity.User;
import backend.domain.user.repository.UserRepository;
import backend.domain.user.service.UserService;
import backend.global.exception.BusinessLogicException;
import backend.global.exception.ExceptionCode;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class RatingService {
	private final UserService userService;
	private final UserRepository userRepository;
	private final BeerRepository beerRepository;
	private final RatingRepository ratingRepository;
	private final RatingMongoRepository ratingMongoRepository;

	public Mono<Rating> findRatingByRatingId(String ratingId) {
		return ratingRepository.findById(ratingId)
			.switchIfEmpty(Mono.error(new RatingNotFoundException()));
	}

	public Mono<List<Rating>> findRatingsByBeerId(String beerId) {
		return ratingMongoRepository.findRatingsByBeerId(beerId);
	}
}
