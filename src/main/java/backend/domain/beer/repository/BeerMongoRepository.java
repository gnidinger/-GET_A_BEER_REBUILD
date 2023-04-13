package backend.domain.beer.repository;

import java.util.Collections;

import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import backend.domain.beer.entity.Beer;
import backend.domain.rating.entity.Rating;
import backend.domain.rating.repository.RatingMongoRepository;
import com.mongodb.client.result.UpdateResult;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class BeerMongoRepository {
	private final ReactiveMongoTemplate reactiveMongoTemplate;
	private final RatingMongoRepository ratingMongoRepository;

	public Mono<Beer> insert(Beer beer) {
		return reactiveMongoTemplate.insert(beer);
	}

	public Mono<Beer> save(Beer beer) {
		return reactiveMongoTemplate.save(beer);
	}

	public Mono<UpdateResult> update(Rating rating, String beerId) {

		System.out.println(rating.getContent());

		return reactiveMongoTemplate.update(Beer.class)
			.matching(Query.query(Criteria.where("_id").is(beerId)))
			.apply(new Update().addToSet("ratingList", rating))
			.first();
	}

	public Mono<Beer> findById(String beerId) {

		return reactiveMongoTemplate.findById(beerId, Beer.class)
			.doOnNext(beer -> {
				ratingMongoRepository.findRatingsByBeerId(beer.getId())
					// .doOnNext(beer::addRatingList)
					.subscribe();
			});
	}

	public Mono<Beer> findByRatingId(String ratingId) {

		Criteria criteria = Criteria.where("ratingList").in(ratingId);
		Query query = Query.query(criteria);
		Mono<Beer> beerMono = reactiveMongoTemplate.findOne(query, Beer.class);

		return beerMono;
	}

}
