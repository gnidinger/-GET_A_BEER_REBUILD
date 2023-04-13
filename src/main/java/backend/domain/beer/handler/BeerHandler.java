package backend.domain.beer.handler;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebInputException;

import backend.domain.beer.dto.BeerRequestDto;
import backend.domain.beer.dto.BeerResponseDto;
import backend.domain.beer.entity.Beer;
import backend.domain.beer.exception.BeerNotFoundException;
import backend.domain.beer.mapper.BeerMapper;
import backend.domain.beer.repository.BeerMongoRepository;
import backend.domain.beer.repository.BeerRepository;
import backend.domain.beer.service.BeerService;
import backend.domain.beer.validator.BeerPostDtoValidator;
import backend.domain.beer.validator.BeerValidator;
import backend.domain.pairing.service.PairingService;
import backend.domain.rating.repository.RatingRepository;
import backend.domain.rating.service.RatingService;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.BufferOverflowStrategy;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

@Component
@RequiredArgsConstructor
public class BeerHandler {
	private final BeerValidator beerValidator;
	private final BeerPostDtoValidator beerPostDtoValidator;
	private final BeerService beerService;
	private final BeerMapper beerMapper;
	private final BeerRepository beerRepository;
	private final BeerMongoRepository beerMongoRepository;
	private final RatingService ratingService;
	private final RatingRepository ratingRepository;
	private final PairingService pairingService;

	public Mono<ServerResponse> createBeer(ServerRequest serverRequest) {

		return serverRequest.bodyToMono(Beer.class)
			// return serverRequest.bodyToMono(BeerRequestDto.Post.class)
			.doOnNext(this::validate)
			// .map(BeerRequestDto.Post::toBeerEntity)
			.flatMap(beerRepository::insert)
			.doOnNext(beer -> beersSink.tryEmitNext(beer))
			.flatMap(savedBeer ->
				ServerResponse.status(HttpStatus.CREATED).bodyValue(savedBeer));
	}

	public Mono<ServerResponse> readBeer(ServerRequest serverRequest) {

		String beerId = serverRequest.pathVariable("beerId");

		Mono<BeerResponseDto.ReadResponse> beerMono = beerService.findBeerByBeerId(beerId)
			.switchIfEmpty(Mono.error(new BeerNotFoundException()))
			.map(beerMapper::beerToReadResponseTemp);

		return ServerResponse.ok().body(beerMono, BeerResponseDto.ReadResponse.class);
	}

	public Mono<ServerResponse> readBeers() {

		Mono<List<Beer>> beerMono = beerRepository.findAll()
			.sort((beer1, beer2) -> Double.compare(beer2.getAbv(), beer1.getAbv()))
			.collectList();

		Flux<Beer> beerFlux = beerRepository.findAll();
		beerFlux.onBackpressureBuffer(256, BufferOverflowStrategy.ERROR);

		return ServerResponse.ok().body(beerMono, Beer.class);
	}

	public Mono<ServerResponse> readBeerStream(ServerRequest serverRequest) {

		return ServerResponse.ok()
			.contentType(MediaType.APPLICATION_NDJSON)
			.body(beersSink.asFlux(), Beer.class)
			.log();
	}

	public Mono<ServerResponse> updateBeer(ServerRequest serverRequest) {

		String beerId = serverRequest.pathVariable("beerId");

		return serverRequest.bodyToMono(Beer.class)
			.doOnNext(this::validate)
			.doOnNext(beer -> {
				Mono<Beer> beerMono = beerRepository.findById(beerId);
				beerMono.doOnNext(beer1 -> beer1.update(beer));
			})
			.flatMap(beerRepository::save)
			.doOnNext(beer -> beersSink.tryEmitNext(beer))
			.flatMap(savedBeer ->
				ServerResponse.status(HttpStatus.CREATED).bodyValue(savedBeer));
	}

	public Mono<ServerResponse> deleteBeer(ServerRequest serverRequest) {

		String beerId = serverRequest.pathVariable("beerId");

		ratingService.findRatingsByBeerId(beerId)
			.flatMap(ratingList -> ratingRepository.deleteAll())
			.subscribe();

		return beerRepository.deleteById(beerId)
			.then(ServerResponse.noContent().build());
	}

	/*
	 * @Valid 커스텀
	 */
	private void validate(Beer beer) {
		Errors errors = new BeanPropertyBindingResult(beer, Beer.class.getName());

		beerValidator.validate(beer, errors);
		if (errors.hasErrors()) {
			throw new ServerWebInputException(errors.toString());
		}
	}

	/*
	 * Dto를 위한 오버로딩
	 */
	private void validate(BeerRequestDto.Post postDto) {
		Errors errors = new BeanPropertyBindingResult(postDto, BeerRequestDto.Post.class.getName());

		beerPostDtoValidator.validate(postDto, errors);
		if (errors.hasErrors()) {
			throw new ServerWebInputException(errors.toString());
		}
	}

	/*
	 * 새로고침시 내 브라우저 올클리어
	 */
	Sinks.Many<Beer> beersSink = Sinks.many().replay().latest();
	Sinks.Many<BeerResponseDto.ReadResponse> readResponseSink = Sinks.many().replay().latest();
}
