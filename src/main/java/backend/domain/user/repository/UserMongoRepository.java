package backend.domain.user.repository;

import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import com.mongodb.client.result.UpdateResult;

import backend.domain.like.entity.Like;
import backend.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class UserMongoRepository {
	private final ReactiveMongoTemplate reactiveMongoTemplate;

	public Mono<UpdateResult> addLike(Like like, String userId) {

		return reactiveMongoTemplate.update(User.class)
			.matching(Query.query(Criteria.where("_id").is(userId)))
			.apply(new Update().push("likeList", like))
			.first();
	}

	public Mono<UpdateResult> removeLike(Like like, String userId) {

		return reactiveMongoTemplate.update(User.class)
			.matching(Query.query(Criteria.where("_id").is(userId)))
			.apply(new Update().pull("likeList", like))
			.first();
	}
}
