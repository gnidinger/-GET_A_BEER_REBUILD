package backend.domain.like.handler;

import java.security.Principal;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import backend.domain.comment.entity.Comment;
import backend.domain.comment.exception.CommentNotFoundException;
import backend.domain.comment.repository.CommentRepository;
import backend.domain.like.entity.Like;
import backend.domain.like.entity.LikeType;
import backend.domain.like.repository.LikeRepository;
import backend.domain.pairing.entity.Pairing;
import backend.domain.pairing.exception.PairingNotFoundException;
import backend.domain.pairing.repository.PairingRepository;
import backend.domain.rating.entity.Rating;
import backend.domain.rating.exception.RatingNotFoundException;
import backend.domain.rating.repository.RatingRepository;
import backend.domain.user.entity.User;
import backend.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class LikeHandler {
	private final UserRepository userRepository;
	private final RatingRepository ratingRepository;
	private final PairingRepository pairingRepository;
	private final CommentRepository commentRepository;
	private final LikeRepository likeRepository;

	@PreAuthorize("hasRole('ROLE_USER')")
	public Mono<ServerResponse> ratingLike(ServerRequest serverRequest) {

		String ratingId = serverRequest.pathVariable("ratingId");

		return ReactiveSecurityContextHolder.getContext()
			.map(SecurityContext::getAuthentication)
			.map(Principal::getName)
			.flatMap(userRepository::findByEmail)
			.flatMap(user -> likeRepository.findByUserIdAndRatingId(user.getId(), ratingId)
				.flatMap(like -> removeRatingLike(ratingId, like, user))
				.switchIfEmpty(addRatingLike(ratingId, user)))
			.flatMap(like -> ServerResponse.ok().bodyValue(like))
			.onErrorResume(e -> ServerResponse.notFound().build());
	}

	private Mono<Like> addRatingLike(String ratingId, User user) {
		return ratingRepository.findById(ratingId)
			.switchIfEmpty(Mono.error(new RatingNotFoundException()))
			.doOnNext(Rating::addLikeCount)
			.flatMap(ratingRepository::save)
			.thenReturn(user)
			.flatMap(findUser -> likeRepository.save(Like.builder()
				.likeType(LikeType.RATING)
				.ratingId(ratingId)
				.userId(findUser.getId())
				.build()))
			.flatMap(like -> {
				user.addLike(like);
				return userRepository.save(user).thenReturn(like);
			});
	}

	private Mono<Like> removeRatingLike(String ratingId, Like like, User user) {
		likeRepository.delete(like).subscribe();
		return ratingRepository.findById(ratingId)
			.switchIfEmpty(Mono.error(new RatingNotFoundException()))
			.doOnNext(Rating::removeLikeCount)
			.flatMap(ratingRepository::save)
			.thenReturn(user.removeLike(like))
			.flatMap(userRepository::save)
			.thenReturn(like);
	}

	@PreAuthorize("hasRole('ROLE_USER')")
	public Mono<ServerResponse> pairingLike(ServerRequest serverRequest) {

		String pairingId = serverRequest.pathVariable("pairingId");

		return ReactiveSecurityContextHolder.getContext()
			.map(SecurityContext::getAuthentication)
			.map(Principal::getName)
			.flatMap(userRepository::findByEmail)
			.flatMap(user -> likeRepository.findByUserIdAndPairingId(user.getId(), pairingId)
				.flatMap(like -> removePairingLike(pairingId, like, user))
				.switchIfEmpty(addPairingLike(pairingId, user)))
			.flatMap(like -> ServerResponse.ok().bodyValue(like))
			.onErrorResume(e -> ServerResponse.notFound().build());
	}

	private Mono<Like> addPairingLike(String pairingId, User user) {
		return pairingRepository.findById(pairingId)
			.switchIfEmpty(Mono.error(new PairingNotFoundException()))
			.doOnNext(Pairing::addLikeCount)
			.flatMap(pairingRepository::save)
			.thenReturn(user)
			.flatMap(findUser -> likeRepository.save(Like.builder()
				.likeType(LikeType.PAIRING)
				.pairingId(pairingId)
				.userId(findUser.getId())
				.build()))
			.flatMap(like -> {
				user.addLike(like);
				return userRepository.save(user).thenReturn(like);
			});
	}

	private Mono<Like> removePairingLike(String pairingId, Like like, User user) {
		likeRepository.delete(like).subscribe();
		return pairingRepository.findById(pairingId)
			.switchIfEmpty(Mono.error(new PairingNotFoundException()))
			.doOnNext(Pairing::removeLikeCount)
			.flatMap(pairingRepository::save)
			.thenReturn(user.removeLike(like))
			.flatMap(userRepository::save)
			.thenReturn(like);
	}

	@PreAuthorize("hasRole('ROLE_USER')")
	public Mono<ServerResponse> commentLike(ServerRequest serverRequest) {

		String commentId = serverRequest.pathVariable("commentId");

		Mono<User> currentUserMono = ReactiveSecurityContextHolder.getContext()
			.map(SecurityContext::getAuthentication)
			.map(Principal::getName)
			.flatMap(userRepository::findByEmail);

		Mono<Like> likeMono = currentUserMono
			.flatMap(user -> likeRepository.findByUserIdAndCommentId(user.getId(), commentId)
				.flatMap(like -> { // 이미 like 객체가 존재하는 경우
					likeRepository.delete(like).subscribe();
					return removeLikeAndUpdateComment(user, like, commentId);
				})
				.switchIfEmpty( // like 객체가 존재하지 않는 경우
					likeRepository.save(
							Like.builder()
								.likeType(LikeType.COMMENT)
								.commentId(commentId)
								.userId(user.getId())
								.build())
						.flatMap(like -> addLikeAndUpdateComment(user, like, commentId))
				)
			);

		return ServerResponse.ok().body(likeMono, Like.class);
	}

	private Mono<Like> addLikeAndUpdateComment(User user, Like like, String commentId) {
		commentRepository.findById(commentId)
			.doOnNext(Comment::addLikeCount)
			.flatMap(commentRepository::save)
			.subscribe();
		user.addLike(like);
		userRepository.save(user).subscribe();
		// userMongoRepository.addLike(like, user.getId()).subscribe();
		return Mono.just(like);
	}

	private Mono<Like> removeLikeAndUpdateComment(User user, Like like, String commentId) {
		commentRepository.findById(commentId)
			.doOnNext(Comment::removeLikeCount)
			.flatMap(commentRepository::save)
			.subscribe();
		user.removeLike(like);
		userRepository.save(user).subscribe();
		// userMongoRepository.removeLike(like, user.getId()).subscribe();
		return Mono.just(like);
	}


	private Mono<Like> addLikeAndUpdatePairing(User user, Like like, String pairingId) {
		pairingRepository.findById(pairingId)
			.switchIfEmpty(Mono.error(new CommentNotFoundException()))
			.doOnNext(Pairing::addLikeCount)
			.flatMap(pairingRepository::save)
			.subscribe();
		user.addLike(like);
		userRepository.save(user).subscribe();
		// userMongoRepository.addLike(like, user.getId()).subscribe();
		return Mono.just(like);
	}

	private Mono<Like> removeLikeAndUpdatePairing(User user, Like like, String pairingId) {
		pairingRepository.findById(pairingId)
			.switchIfEmpty(Mono.error(new CommentNotFoundException()))
			.doOnNext(Pairing::removeLikeCount)
			.flatMap(pairingRepository::save)
			.subscribe();
		user.removeLike(like);
		userRepository.save(user).subscribe();
		// userMongoRepository.removeLike(like, user.getId()).subscribe();
		return Mono.just(like);
	}


}
