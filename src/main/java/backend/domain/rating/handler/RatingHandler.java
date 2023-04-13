package backend.domain.rating.handler;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebInputException;

import backend.domain.beer.exception.BeerNotFoundException;
import backend.domain.beer.repository.BeerMongoRepository;
import backend.domain.beer.repository.BeerRepository;
import backend.domain.beer.service.BeerService;
import backend.domain.rating.entity.Rating;
import backend.domain.rating.exception.RatingNotFoundException;
import backend.domain.rating.exception.UserNotMatchException;
import backend.domain.rating.repository.RatingMongoRepository;
import backend.domain.rating.repository.RatingRepository;
import backend.domain.rating.service.RatingService;
import backend.domain.rating.validator.RatingValidator;
import backend.domain.user.entity.User;
import backend.domain.user.repository.UserRepository;
import backend.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

@Component
@RequiredArgsConstructor
public class RatingHandler {
	private final UserService userService;
	private final UserRepository userRepository;
	private final BeerService beerService;
	private final BeerRepository beerRepository;
	private final BeerMongoRepository beerMongoRepository;
	private final RatingValidator ratingValidator;
	private final RatingService ratingService;
	private final RatingRepository ratingRepository;
	private final RatingMongoRepository ratingMongoRepository;

	@PreAuthorize("hasRole('ROLE_USER')")
	public Mono<ServerResponse> createRating(ServerRequest serverRequest) {

		String beerId = serverRequest.pathVariable("beerId");

		Mono<User> currentUserMono = userService.getCurrentUser();

		Mono<Rating> ratingMono = currentUserMono
			.flatMap(user ->
				serverRequest.bodyToMono(Rating.class)
					.doOnNext(rating -> validate(rating, beerId))
					.flatMap(rating -> beerService.findBeerByBeerId(beerId)
						.map(beer -> {
							if (beer == null) {
								throw new BeerNotFoundException();
							} else {
								rating.addUserId(user.getId());
								return rating;
							}
						}))
					.flatMap(rating -> {
						return beerRepository.findById(beerId)
							.flatMap(beer -> {

								Double ratingStar = rating.getStar();

								beer.addTotalAverageStar(ratingStar);

								switch (user.getGenderType()) {
									case "FEMALE":
										beer.addFemaleAverageStar(ratingStar);
										break;
									case "MALE":
										beer.addMaleAverageStar(ratingStar);
										break;
									case "OTHERS":
										beer.addOthersAverageStar(ratingStar);
										break;
								}

								return beerMongoRepository.save(beer.updateBeerTag(rating));
							})
							.flatMap(beer -> ratingMongoRepository.insert(rating, beerId))
							.flatMap(rating1 -> {
								user.addRatingId(rating1.getId());
								return userRepository.save(user)
									.thenReturn(rating1);
							});
					}));

		return ServerResponse.status(HttpStatus.CREATED).body(ratingMono, Rating.class);
	}

	public Mono<ServerResponse> readRating(ServerRequest serverRequest) {

		String ratingId = serverRequest.pathVariable("ratingId");

		Mono<Rating> ratingMono = ratingService.findRatingByRatingId(ratingId);

		return ServerResponse.ok().body(ratingMono, Rating.class);
	}

	public Mono<ServerResponse> readRatings() {

		Mono<List<Rating>> listMono = ratingRepository.findAll()
			.sort((rating1, rating2) -> Double.compare(rating2.getStar(), rating1.getStar()))
			.collectList();

		return ServerResponse.ok().body(listMono, Rating.class);
	}

	public Mono<Page<Rating>> readRatingPageMono(ServerRequest serverRequest, String sort, int page) {

		String beerId = serverRequest.pathVariable("beerId");

		PageRequest pageRequest = PageRequest.of(page - 1, 10);

		return ratingMongoRepository.findRatingsPageByBeerId(beerId, sort, pageRequest);
	}

	@PreAuthorize("hasRole('ROLE_USER')")
	public Mono<ServerResponse> updateRating(ServerRequest serverRequest) {

		String ratingId = serverRequest.pathVariable("ratingId");

		Mono<User> currentUserMono = userService.getCurrentUser();

		ratingRepository.findById(ratingId)
			.flatMap(rating -> {
				return beerRepository.findById(rating.getBeerId())
					.doOnNext(beer -> beer.deleteBeerTag(rating))
					.flatMap(beerMongoRepository::save);
			})
			.subscribe();

		Mono<Rating> ratingMono = currentUserMono
			.flatMap(user ->
				serverRequest.bodyToMono(Rating.class)
					.doOnNext(this::validate)
					.flatMap(rating ->
						ratingService.findRatingByRatingId(ratingId)
							.map(rating1 -> {
								if (rating1 == null) {
									throw new RatingNotFoundException();
								} else if (!rating1.getUserId().equals(user.getId())) {
									throw new UserNotMatchException();
								} else {
									return rating1;
								}
							})
							.flatMap(rating1 -> {
								Double currentStar = rating1.getStar();
								Double updateStar = rating.getStar();
								rating1.update(rating, ratingId);
								ratingMongoRepository.save(rating1).subscribe();
								beerRepository.findById(rating1.getBeerId())
									.flatMap(beer -> {
										beer.removeTotalAverageStar(currentStar);
										beer.addTotalAverageStar(updateStar);

										switch (user.getGenderType()) {
											case "FEMALE":
												beer.removeFemaleAverageStar(currentStar);
												beer.addFemaleAverageStar(updateStar);
												break;
											case "MALE":
												beer.removeMaleAverageStar(currentStar);
												beer.addMaleAverageStar(updateStar);
												break;
											case "OTHERS":
												beer.removeOthersAverageStar(currentStar);
												beer.addOthersAverageStar(updateStar);
												break;
										}

										beer.updateBeerTag(rating1);
										return beerRepository.save(beer);
									}).subscribe();
								return Mono.just(rating1);
							})
							.doOnNext(rating1 -> ratingsSink.tryEmitNext(rating1))
					));

		return ServerResponse.ok().body(ratingMono, Rating.class);
	}

	@PreAuthorize("hasRole('ROLE_USER')")
	public Mono<ServerResponse> deleteRating(ServerRequest serverRequest) {

		String ratingId = serverRequest.pathVariable("ratingId");

		Mono<User> currentUserMono = userService.getCurrentUser();

		Mono<Rating> ratingMono = ratingService.findRatingByRatingId(ratingId)
			.switchIfEmpty(Mono.error(new RatingNotFoundException()));

		Mono<Void> deleteRatingMono = Mono.zip(currentUserMono, ratingMono)
			.flatMap(tuple -> {
				User currentUser = tuple.getT1();
				Rating rating = tuple.getT2();

				if (!rating.getUserId().equals(currentUser.getId())) {
					return Mono.error(new UserNotMatchException());
				}

				currentUser.removeRatingId(rating.getId());
				userRepository.save(currentUser).subscribe();

				return beerRepository.findById(rating.getBeerId())
					.flatMap(beer -> {
						beer.removeTotalAverageStar(rating.getStar());

						switch (currentUser.getGenderType()) {
							case "FEMALE":
								beer.removeFemaleAverageStar(rating.getStar());
								break;
							case "MALE":
								beer.removeMaleAverageStar(rating.getStar());
								break;
							case "OTHERS":
								beer.removeOthersAverageStar(rating.getStar());
								break;
						}

						beer.deleteBeerTag(rating);
						beer.deleteRatingId(rating.getId());
						return beerMongoRepository.save(beer);
					})
					.then(ratingRepository.delete(rating));
			});

		return deleteRatingMono.then(ServerResponse.noContent().build());
	}

	/*
	 * @Valid 커스텀
	 */
	private void validate(Rating rating) {
		Errors errors = new BeanPropertyBindingResult(rating, Rating.class.getName());

		ratingValidator.validate(rating, errors);
		if (errors.hasErrors()) {
			throw new ServerWebInputException(errors.toString());
		}
	}

	private void validate(Rating rating, String beerId) {
		Errors errors = new BeanPropertyBindingResult(rating, Rating.class.getName());

		ratingValidator.validate(rating, beerId, errors);
		if (errors.hasErrors()) {
			throw new ServerWebInputException(errors.toString());
		}
	}

	/*
	 * 새로고침시 내 브라우저 올클리어
	 */
	Sinks.Many<Rating> ratingsSink = Sinks.many().replay().latest();
}
