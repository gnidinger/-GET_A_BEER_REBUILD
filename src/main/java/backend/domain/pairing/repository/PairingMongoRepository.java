package backend.domain.pairing.repository;

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
import backend.domain.pairing.entity.Pairing;

import backend.domain.rating.entity.Rating;
import backend.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class PairingMongoRepository {
	private final ReactiveMongoTemplate reactiveMongoTemplate;

	public Mono<Pairing> insert(Pairing createdPairing, String beerId) {

		createdPairing.addBeerId(beerId);

		return reactiveMongoTemplate.insert(createdPairing)
			.doOnNext(pairing -> {
				reactiveMongoTemplate.update(Beer.class)
					.matching(Query.query(Criteria.where("id").is(beerId)))
					.apply(new Update().push("pairingList").value(pairing.getId()))
					.first().subscribe();
			});
	}

	public Mono<Pairing> insert(Pairing createdPairing, User findUser) {

		// createdPairing.addUserId(findUser.getId());

		return reactiveMongoTemplate.insert(createdPairing)
			.doOnNext(pairing -> {
				reactiveMongoTemplate.update(User.class)
					.matching(Query.query(Criteria.where("id").is(findUser.getId())))
					.apply(new Update().push("pairingList").value(pairing.getId()))
					.first().subscribe();
			});
	}

	public Mono<List<Pairing>> findPairingsByBeerId(String beerId) {

		return reactiveMongoTemplate
			.find(Query.query(Criteria.where("beerId").is(beerId)), Pairing.class)
			.sort(Comparator.comparing(Pairing::getCreatedAt))
			.collectList();
	}

	public Mono<Pairing> save(Pairing pairing) {
		return reactiveMongoTemplate.save(pairing);
	}

	public Mono<Page<Pairing>> findPairingsPageByBeerId(
		String beerId, String category, String querySort, Pageable pageable) {

		Criteria criteria = Criteria.where("beerId").is(beerId);

		if (category != null) {
			criteria.and("category").is(category);
		}

		Query query = Query.query(criteria).with(pageable);

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

		Mono<Long> countMono = reactiveMongoTemplate.count(query, Pairing.class);
		Mono<List<Pairing>> pairingsMono = reactiveMongoTemplate.find(query, Pairing.class).collectList();

		return Mono.zip(countMono, pairingsMono)
			.map(tuple -> new PageImpl<>(tuple.getT2(), pageable, tuple.getT1()));
	}
}
