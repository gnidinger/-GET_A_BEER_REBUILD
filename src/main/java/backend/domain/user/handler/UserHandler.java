package backend.domain.user.handler;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebInputException;

import backend.domain.user.dto.UserRequestDto;
import backend.domain.user.entity.User;
import backend.domain.user.repository.UserRepository;
import backend.domain.user.service.UserService;
import backend.domain.user.validator.UserFirstSigninValidator;
import backend.domain.user.validator.UserSignupValidator;
import backend.domain.user.validator.UserValidator;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

@Component
@RequiredArgsConstructor
public class UserHandler {
	private final UserValidator userValidator;
	private final UserSignupValidator userSignupValidator;
	private final UserFirstSigninValidator userFirstSigninValidator;
	private final UserService userService;
	private final UserRepository userRepository;
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

	public Mono<ServerResponse> firstSignin(ServerRequest serverRequest) {

		String userId = serverRequest.pathVariable("userId");

		return serverRequest.bodyToMono(UserRequestDto.FirstSignIn.class)
			.doOnNext(this::validate)
			.flatMap(firstSignIn -> {
				return userRepository.findById(userId)
					.map(user -> user.firstSignin(firstSignIn));
			})
			.flatMap(userRepository::save)
			.doOnNext(user -> usersSink.tryEmitNext(user))
			.flatMap(savedUser ->
				ServerResponse.status(HttpStatus.OK).bodyValue(savedUser));
	}

	// public Mono<ServerResponse> signin(ServerRequest serverRequest) {
	//
	// 	return serverRequest.bodyToMono(SigninDto.Request.class)
	// 		.flatMap(signinService::signin)
	// 		.flatMap(response ->
	// 			ServerResponse.status(HttpStatus.OK).bodyValue(response));
	// }

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
		System.out.println("#####" + serverRequest.path());
		serverRequest.principal()
			.doOnNext(principal -> System.out.println(principal.getName()))
			.doOnNext(principal -> System.out.println(principal.toString()))
			.doOnNext(principal -> System.out.println("ASDF"))
			.subscribe();
		System.out.println("#######################################");
		serverRequest.session()
			.doOnNext(webSession -> System.out.println(webSession.getId()))
			.doOnNext(webSession -> System.out.println(webSession.getCreationTime()))
			.subscribe();
		System.out.println("#####" + serverRequest.path());
		return null;
		// return serverRequest.principal()
		// 	.flatMap(principal -> ServerResponse.ok().body(fromObject(principal.getName()), String.class));
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

	private void validate(UserRequestDto.FirstSignIn firstSignIn) {
		Errors errors = new BeanPropertyBindingResult(firstSignIn, UserRequestDto.FirstSignIn.class.getName());

		userFirstSigninValidator.validate(firstSignIn, errors);
		if (errors.hasErrors()) {
			throw new ServerWebInputException(errors.toString());
		}
	}

	/*
	 * 새로고침시 내 브라우저 올클리어
	 */
	Sinks.Many<User> usersSink = Sinks.many().replay().latest();
}
