package backend.domain.user.handler;

import java.security.Principal;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebInputException;

import backend.domain.beer.repository.BeerMongoRepository;
import backend.domain.beer.repository.BeerRepository;
import backend.domain.rating.entity.Rating;
import backend.domain.rating.repository.RatingMongoRepository;
import backend.domain.rating.repository.RatingRepository;
import backend.domain.user.dto.UserRequestDto;
import backend.domain.user.entity.User;
import backend.domain.user.repository.UserRepository;
import backend.domain.user.service.UserService;
import backend.domain.user.validator.UserFirstSigninRequestValidator;
import backend.domain.user.validator.UserSignupValidator;
import backend.domain.user.validator.UserUpdateInfoValidator;
import backend.domain.user.validator.UserValidator;
import backend.global.exception.BusinessLogicException;
import backend.global.exception.ExceptionCode;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

@Component
@RequiredArgsConstructor
public class UserHandler {
	private final UserValidator userValidator;
	private final UserSignupValidator userSignupValidator;
	private final UserFirstSigninRequestValidator userFirstSigninRequestValidator;
	private final UserUpdateInfoValidator userUpdateInfoValidator;
	private final UserService userService;
	private final UserRepository userRepository;
	private final BeerRepository beerRepository;
	private final BeerMongoRepository beerMongoRepository;
	private final RatingRepository ratingRepository;
	private final RatingMongoRepository ratingMongoRepository;
	private final PasswordEncoder passwordEncoder;

	public Mono<ServerResponse> signup(ServerRequest serverRequest) {

		return serverRequest.bodyToMono(UserRequestDto.SignUp.class)
			.doOnNext(this::validate)
			.map(signUp -> signUp.toUser(passwordEncoder))
			.flatMap(userRepository::insert)
			.doOnNext(user -> usersSink.tryEmitNext(user))
			.flatMap(savedUser ->
				ServerResponse.status(HttpStatus.CREATED).bodyValue(savedUser));
	}

	@PreAuthorize("hasRole('ROLE_USER')")
	public Mono<ServerResponse> firstSignin(ServerRequest serverRequest) {

		String userId = serverRequest.pathVariable("userId");

		return ReactiveSecurityContextHolder.getContext()
			.map(SecurityContext::getAuthentication)
			.map(Principal::getName)
			.flatMap(userRepository::findByEmail)
			.flatMap(user -> {
				// serverRequest.bodyToMono(UserRequestDto.FirstSigninRequest.class);
				if (user.getId().equals(userId)) {
					return serverRequest.bodyToMono(UserRequestDto.FirstSigninRequest.class)
						.doOnNext(this::validate)
						.flatMap(updateInfoRequest -> userService.firstSignin(updateInfoRequest, user));
				} else {
					return Mono.error(new BusinessLogicException(ExceptionCode.UNAUTHORIZED));
				}
			})
			.flatMap(firstSigninResponse -> ServerResponse.ok().bodyValue(firstSigninResponse));
	}

	public Mono<ServerResponse> readUser(ServerRequest serverRequest) {

		String userId = serverRequest.pathVariable("userId");

		Mono<User> userMono = userService.findUserByUserId(userId);

		return ServerResponse.ok().body(userMono, User.class);
	}

	public Mono<ServerResponse> readUsers() {
		return null;
	}

	@PreAuthorize("hasRole('ROLE_USER')")
	public Mono<ServerResponse> updateUser(ServerRequest serverRequest) {

		String userId = serverRequest.pathVariable("userId");

		return ReactiveSecurityContextHolder.getContext()
			.map(SecurityContext::getAuthentication)
			.map(Principal::getName)
			.flatMap(userRepository::findByEmail)
			.flatMap(user -> {
				serverRequest.bodyToMono(UserRequestDto.UpdateInfoRequest.class);
				if (user.getId().equals(userId)) {
					return serverRequest.bodyToMono(UserRequestDto.UpdateInfoRequest.class)
						.doOnNext(this::validate)
						.flatMap(updateInfoRequest -> {
							if (!user.getNickname().equals(updateInfoRequest.getNickname())
								&& !userService.findUserByNickname(updateInfoRequest.getNickname()).isDisposed()) {
								return Mono.error(new BusinessLogicException(ExceptionCode.NICKNAME_EXIST));
							}
							return Mono.just(updateInfoRequest);
						})
						.flatMap(updateInfoRequest -> {
							// Mono<List<Rating>> ratingList = ratingMongoRepository.findRatingListByUserId(userId);
							Flux<Rating> ratingFlux = ratingRepository.findByUserId(userId);
							return calculateStars(user, updateInfoRequest, ratingFlux)
								.then(userService.updateUserInfo(updateInfoRequest, user));
						});
				} else {
					return Mono.error(new BusinessLogicException(ExceptionCode.UNAUTHORIZED));
				}
			})
			.flatMap(updateInfoResponse -> ServerResponse.ok().bodyValue(updateInfoResponse));
	}

	private Mono<Void> calculateStars(
		User user, UserRequestDto.UpdateInfoRequest updateInfoRequest, Flux<Rating> ratingList) {

		ratingList.flatMap(rating -> {
			Double currentStar = rating.getStar();
			return beerMongoRepository.findByRatingId(rating.getId())
				.flatMap(beer -> {
					if (!user.getGenderType().equals(updateInfoRequest.getGenderType())) {
						switch (user.getGenderType()) {
							case "FEMALE":
								if (updateInfoRequest.getGenderType().equals("MALE")) {
									beer.removeFemaleAverageStar(currentStar);
									beer.addMaleAverageStar(currentStar);
								} else if (updateInfoRequest.getGenderType().equals("OTHERS")) {
									beer.removeFemaleAverageStar(currentStar);
									beer.addOthersAverageStar(currentStar);
								} else { // NONE
									beer.removeFemaleAverageStar(currentStar);
									beer.addFemaleAverageStar(currentStar);
								}
								break;
							case "MALE":
								if (updateInfoRequest.getGenderType().equals("FEMALE")) {
									beer.removeMaleAverageStar(currentStar);
									beer.addFemaleAverageStar(currentStar);
								} else if (updateInfoRequest.getGenderType().equals("OTHERS")) {
									beer.removeMaleAverageStar(currentStar);
									beer.addOthersAverageStar(currentStar);
								} else { // NONE
									beer.removeMaleAverageStar(currentStar);
									beer.addMaleAverageStar(currentStar);
								}
								break;
							case "OTHERS":
								if (updateInfoRequest.getGenderType().equals("FEMALE")) {
									beer.removeOthersAverageStar(currentStar);
									beer.addFemaleAverageStar(currentStar);
								} else if (updateInfoRequest.getGenderType().equals("MALE")) {
									beer.removeOthersAverageStar(currentStar);
									beer.addMaleAverageStar(currentStar);
								} else { // NONE
									beer.removeOthersAverageStar(currentStar);
								}
								break;
							case "NONE":
								if (updateInfoRequest.getGenderType().equals("FEMALE")) {
									beer.addFemaleAverageStar(currentStar);
								} else if (updateInfoRequest.getGenderType().equals("MALE")) {
									beer.addMaleAverageStar(currentStar);
								} else {
									beer.addOthersAverageStar(currentStar);
								}
						}
					}
					return beerRepository.save(beer).then();
				});
		}).subscribe();
		return Mono.empty();
	}

	public Mono<ServerResponse> deleteUser(ServerRequest serverRequest) {
		return null;
	}

	/*
	 * @Valid 커스텀
	 */
	private void validate(User user) {
		Errors errors = new BeanPropertyBindingResult(user, User.class.getName());

		userValidator.validate(user, errors);
		if (errors.hasErrors()) {
			throw new ServerWebInputException(errors.toString());
		}
	}

	private void validate(UserRequestDto.SignUp signUp) {
		Errors errors = new BeanPropertyBindingResult(signUp, UserRequestDto.SignUp.class.getName());

		userSignupValidator.validate(signUp, errors);
		if (errors.hasErrors()) {
			throw new ServerWebInputException(errors.toString());
		}
	}

	private void validate(UserRequestDto.FirstSigninRequest firstSignIn) {
		Errors errors = new BeanPropertyBindingResult(firstSignIn, UserRequestDto.FirstSigninRequest.class.getName());

		userFirstSigninRequestValidator.validate(firstSignIn, errors);
		if (errors.hasErrors()) {
			throw new ServerWebInputException(errors.toString());
		}
	}

	private void validate(UserRequestDto.UpdateInfoRequest updateInfoRequest) {
		Errors errors = new BeanPropertyBindingResult(updateInfoRequest,
			UserRequestDto.UpdateInfoRequest.class.getName());

		userUpdateInfoValidator.validate(updateInfoRequest, errors);
		if (errors.hasErrors()) {
			throw new ServerWebInputException(errors.toString());
		}
	}

	/*
	 * 새로고침시 내 브라우저 올클리어
	 */
	Sinks.Many<User> usersSink = Sinks.many().replay().latest();
}
