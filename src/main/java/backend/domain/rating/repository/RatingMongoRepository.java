package backend.domain.rating.repository;

import java.util.Comparator;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import backend.domain.beer.entity.Beer;
import backend.domain.rating.entity.Rating;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class RatingMongoRepository {

	private final ReactiveMongoTemplate reactiveMongoTemplate;

	public Mono<Rating> insert(Rating createdRating, String beerId) {

		createdRating.addBeerId(beerId);

		Mono<Rating> resultMono = reactiveMongoTemplate.insert(createdRating)
			.doOnNext(rating -> {
				reactiveMongoTemplate.update(Beer.class)
					.matching(Query.query(Criteria.where("id").is(beerId)))
					.apply(new Update().push("ratingList").value(rating.getId()))
					.first().subscribe();
			});
		return resultMono;
	}

	public Mono<Rating> save(Rating rating) {
		return reactiveMongoTemplate.save(rating);
	}

	public Mono<List<Rating>> findRatingsByBeerId(String beerId) {

		return reactiveMongoTemplate
			.find(Query.query(Criteria.where("beerId").is(beerId)), Rating.class)
			.sort(Comparator.comparing(Rating::getCreatedAt))
			.collectList();
	}

	public Mono<Page<Rating>> findRatingsPageByBeerId(String beerId, String querySort, Pageable pageable) {

		Query query = Query.query(Criteria.where("beerId").is(beerId)).with(pageable);

		if (querySort == null) {
			querySort = "new";
		}

		Sort sort = null;

		switch (querySort) {
			case "new":
				sort = Sort.by(Sort.Direction.DESC, "createdAt");
				break;
			case "likes":
				sort = Sort.by(Sort.Direction.DESC, "likeCount");
				break;
			case "comments":
				sort = Sort.by(Sort.Direction.DESC, "commentCount");
				break;
		}

		query.with(sort);

		Mono<Long> countMono = reactiveMongoTemplate.count(query, Rating.class);
		Mono<List<Rating>> ratingsMono = reactiveMongoTemplate.find(query, Rating.class).collectList();

		return Mono.zip(countMono, ratingsMono)
			.map(tuple -> new PageImpl<>(tuple.getT2(), pageable, tuple.getT1()));
	}

	public Mono<List<Rating>> findRatingListByUserId(String userId) {

		Criteria criteria = Criteria.where("userId").is(userId);

		return reactiveMongoTemplate
			.find(Query.query(criteria), Rating.class)
			.collectList();
	}
}
