package backend.domain.comment.repository;

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

import backend.domain.comment.entity.Comment;
import backend.domain.constant.CommentType;
import backend.domain.pairing.entity.Pairing;
import backend.domain.rating.entity.Rating;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class CommentMongoRepository {
	private final ReactiveMongoTemplate reactiveMongoTemplate;

	public Mono<Comment> insertRating(Comment createdComment, String ratingId) {

		createdComment.addRatingId(ratingId);
		createdComment.addCommentType(CommentType.COMMENT_TYPE_RATING);

		Mono<Comment> resultMono = reactiveMongoTemplate.insert(createdComment)
			.doOnNext(comment -> {
				reactiveMongoTemplate.update(Rating.class)
					.matching(Query.query(Criteria.where("id").is(ratingId)))
					.apply(new Update().push("commentList").value(comment.getId()))
					.first().subscribe();
			});
		return resultMono;
	}

	public Mono<Comment> insertPairing(Comment createdComment, String pairingId) {

		createdComment.addPairingId(pairingId);
		createdComment.addCommentType(CommentType.COMMENT_TYPE_PAIRING);

		Mono<Comment> resultMono = reactiveMongoTemplate.insert(createdComment)
			.doOnNext(Comment -> {
				reactiveMongoTemplate.update(Pairing.class)
					.matching(Query.query(Criteria.where("id").is(pairingId)))
					.apply(new Update().push("commentList").value(Comment.getId()))
					.first().subscribe();
			});
		return resultMono;
	}

	public Mono<Comment> save(Comment comment) {
		return reactiveMongoTemplate.save(comment);
	}

	public Mono<Page<Comment>> findRatingCommentsPageByRatingId(String ratingId, Pageable pageable) {

		Query query = Query.query(Criteria.where("ratingId").regex(ratingId)).with(pageable);

		Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
		query.with(sort);

		Mono<Long> countMono = reactiveMongoTemplate.count(query, Comment.class);
		Mono<List<Comment>> commentsMono = reactiveMongoTemplate.find(query, Comment.class).collectList();

		return Mono.zip(countMono, commentsMono)
			.map(tuple -> new PageImpl<>(tuple.getT2(), pageable, tuple.getT1()));

	}

	public Mono<Page<Comment>> findPairingCommentsPageByRatingId(String pairingId, Pageable pageable) {

		Query query = Query.query(Criteria.where("pairingId").regex(pairingId)).with(pageable);

		Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
		query.with(sort);

		Mono<Long> countMono = reactiveMongoTemplate.count(query, Comment.class);
		Mono<List<Comment>> commentsMono = reactiveMongoTemplate.find(query, Comment.class).collectList();

		return Mono.zip(countMono, commentsMono)
			.map(tuple -> new PageImpl<>(tuple.getT2(), pageable, tuple.getT1()));

	}
}
