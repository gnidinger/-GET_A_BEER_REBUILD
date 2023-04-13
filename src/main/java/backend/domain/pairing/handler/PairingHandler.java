package backend.domain.pairing.handler;

import java.util.Comparator;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
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
import backend.domain.pairing.entity.Pairing;
import backend.domain.pairing.exception.PairingNotFoundException;
import backend.domain.pairing.repository.PairingMongoRepository;
import backend.domain.pairing.repository.PairingRepository;
import backend.domain.pairing.service.PairingService;
import backend.domain.pairing.validator.PairingValidator;
import backend.domain.rating.exception.UserNotMatchException;
import backend.domain.user.entity.User;
import backend.domain.user.exception.UserNotFoundException;
import backend.domain.user.repository.UserRepository;
import backend.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

@Component
@RequiredArgsConstructor
public class PairingHandler {
	private final UserService userService;
	private final UserRepository userRepository;
	private final BeerService beerService;
	private final BeerRepository beerRepository;
	private final BeerMongoRepository beerMongoRepository;
	private final PairingValidator pairingValidator;
	private final PairingService pairingService;
	private final PairingRepository pairingRepository;
	private final PairingMongoRepository pairingMongoRepository;

	public Mono<ServerResponse> createPairing(ServerRequest serverRequest) {

		String beerId = serverRequest.pathVariable("beerId");

		Mono<User> currentUserMono = userService.getCurrentUser();

		Mono<Pairing> pairingMono = currentUserMono
			.zipWith(serverRequest.bodyToMono(Pairing.class), (user, pairing) -> {
				validate(pairing);
				pairing.addUserId(user.getId());
				return pairing;
			})
			.flatMap(pairing -> beerService.findBeerByBeerId(beerId)
				.switchIfEmpty(Mono.error(new BeerNotFoundException()))
				.flatMap(beer -> beerMongoRepository.save(beer.updatePairingCategory(pairing)))
				.flatMap(beer -> pairingMongoRepository.insert(pairing, beerId))
			)
			.flatMap(pairing -> userRepository.findById(pairing.getUserId())
				.switchIfEmpty(Mono.error(new UserNotFoundException()))
				.doOnSuccess(user -> {
					user.addPairingId(pairing.getId());
					userRepository.save(user).subscribe();
				})
				.thenReturn(pairing)
			);

		return ServerResponse.status(HttpStatus.CREATED).body(pairingMono, Pairing.class);
	}

	public Mono<ServerResponse> readPairing(ServerRequest serverRequest) {

		String pairingId = serverRequest.pathVariable("pairingId");

		Mono<Pairing> pairingMono = pairingService.findPairingByPairingId(pairingId);

		return ServerResponse.ok().body(pairingMono, Pairing.class);
	}

	public Mono<ServerResponse> readPairings() {

		Mono<List<Pairing>> listMono = pairingRepository.findAll()
			.sort(Comparator.comparing(Pairing::getCreatedAt))
			.collectList();

		return ServerResponse.ok().body(listMono, Pairing.class);
	}

	public Mono<Page<Pairing>> readPairingPageMono(
		ServerRequest serverRequest, String category, String sort, int page) {

		String beerId = serverRequest.pathVariable("beerId");

		PageRequest pageRequest = PageRequest.of(page - 1, 10);

		return pairingMongoRepository.findPairingsPageByBeerId(beerId, category, sort, pageRequest);
	}

	public Mono<ServerResponse> updatePairing(ServerRequest serverRequest) {

		String pairingId = serverRequest.pathVariable("pairingId");

		Mono<User> currentUserMono = userService.getCurrentUser();

		Mono<Pairing> pairingMono = pairingService.findPairingByPairingId(pairingId)
			.switchIfEmpty(Mono.error(new PairingNotFoundException()));

		Mono<Pairing> updatePairing = Mono.zip(currentUserMono, pairingMono)
			.flatMap(tuple -> {
				User currentUser = tuple.getT1();
				Pairing currentPairing = tuple.getT2();

				if (!currentPairing.getUserId().equals(currentUser.getId())) {
					return Mono.error(new UserNotMatchException());
				}

				return serverRequest.bodyToMono(Pairing.class)
					.doOnNext(this::validate)
					.flatMap(savedPairing -> {
						return beerRepository.findById(currentPairing.getBeerId())
							.flatMap(beer -> {
								beer.deletePairingCategory(currentPairing);
								currentPairing.update(savedPairing);
								pairingRepository.save(currentPairing).subscribe();
								beer.updatePairingCategory(savedPairing);
								return beerMongoRepository.save(beer);
							})
							.thenReturn(savedPairing);
					});
			});

		return ServerResponse.ok().body(updatePairing, Pairing.class);
	}

	public Mono<ServerResponse> deletePairing(ServerRequest serverRequest) {

		String pairingId = serverRequest.pathVariable("pairingId");

		Mono<User> currentUserMono = userService.getCurrentUser();

		Mono<Pairing> pairingMono = pairingService.findPairingByPairingId(pairingId)
			.switchIfEmpty(Mono.error(new PairingNotFoundException()));

		Mono<Void> deletePairingMono = Mono.zip(currentUserMono, pairingMono)
			.flatMap(tuple -> {
				User currentUser = tuple.getT1();
				Pairing currentPairing = tuple.getT2();

				if (!currentPairing.getUserId().equals(currentUser.getId())) {
					return Mono.error(new UserNotMatchException());
				}

				currentUser.removePairingId(currentPairing.getId());
				userRepository.save(currentUser).subscribe();

				return beerRepository.findById(currentPairing.getBeerId())
					.flatMap(beer -> {
						beer.deletePairingCategory(currentPairing);
						beer.deletePairingId(currentPairing.getId());
						return beerRepository.save(beer);
					})
					.then(pairingRepository.delete(currentPairing));
			});

		return deletePairingMono.then(ServerResponse.noContent().build());
	}

	/*
	 * @Valid 커스텀
	 */
	private void validate(Pairing pairing) {
		Errors errors = new BeanPropertyBindingResult(pairing, Pairing.class.getName());

		pairingValidator.validate(pairing, errors);
		if (errors.hasErrors()) {
			throw new ServerWebInputException(errors.toString());
		}
	}

	Sinks.Many<Pairing> pairingsSink = Sinks.many().replay().latest();

}
