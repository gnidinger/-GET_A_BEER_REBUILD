package backend.domain.comment.handler;

import java.security.Principal;
import java.util.Comparator;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebInputException;

import backend.domain.comment.entity.Comment;
import backend.domain.comment.exception.CommentNotFoundException;
import backend.domain.comment.repository.CommentMongoRepository;
import backend.domain.comment.repository.CommentRepository;
import backend.domain.comment.service.CommentService;
import backend.domain.comment.validator.CommentValidator;
import backend.domain.constant.CommentType;
import backend.domain.pairing.entity.Pairing;
import backend.domain.pairing.exception.PairingNotFoundException;
import backend.domain.pairing.repository.PairingRepository;
import backend.domain.pairing.service.PairingService;
import backend.domain.rating.entity.Rating;
import backend.domain.rating.exception.RatingNotFoundException;
import backend.domain.rating.exception.UserNotMatchException;
import backend.domain.rating.repository.RatingRepository;
import backend.domain.rating.service.RatingService;
import backend.domain.user.entity.User;
import backend.domain.user.exception.UserNotFoundException;
import backend.domain.user.repository.UserRepository;
import backend.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

@Component
@RequiredArgsConstructor
public class CommentHandler {
	private final UserService userService;
	private final UserRepository userRepository;
	private final CommentValidator commentValidator;
	private final CommentService commentService;
	private final RatingService ratingService;
	private final RatingRepository ratingRepository;
	private final PairingService pairingService;
	private final PairingRepository pairingRepository;
	private final CommentRepository commentRepository;
	private final CommentMongoRepository commentMongoRepository;

	public Mono<ServerResponse> createRatingComment(ServerRequest serverRequest) {

		String ratingId = serverRequest.pathVariable("ratingId");

		Mono<User> currentUserMono = userService.getCurrentUser();

		Mono<Comment> commentMono = currentUserMono
			.zipWith(serverRequest.bodyToMono(Comment.class), (user, comment) -> {
				validate(comment);
				comment.addUserId(user.getId());
				return comment;
			})
			.flatMap(comment -> ratingService.findRatingByRatingId(ratingId)
				.switchIfEmpty(Mono.error(new RatingNotFoundException()))
				.flatMap(rating -> {
					rating.addCommentCount();
					return ratingRepository.save(rating)
						.then(commentMongoRepository.insertRating(comment, rating.getId()));
				})
			)
			.flatMap(comment -> userRepository.findById(comment.getUserId())
				.switchIfEmpty(Mono.error(new UserNotFoundException()))
				.doOnSuccess(user -> {
					user.addCommentId(comment.getId());
					userRepository.save(user).subscribe();
				})
				.thenReturn(comment)
			);

		return ServerResponse.status(HttpStatus.CREATED).body(commentMono, Comment.class);
	}

	public Mono<ServerResponse> createPairingComment(ServerRequest serverRequest) {

		String pairingId = serverRequest.pathVariable("pairingId");

		Mono<User> currentUserMono = userService.getCurrentUser();

		Mono<Comment> commentMono = currentUserMono
			.zipWith(serverRequest.bodyToMono(Comment.class), (user, comment) -> {
				validate(comment);
				comment.addUserId(user.getId());
				return comment;
			})
			.flatMap(comment -> pairingService.findPairingByPairingId(pairingId)
				.switchIfEmpty(Mono.error(new PairingNotFoundException()))
				.flatMap(pairing -> {
					pairing.addCommentCount();
					return pairingRepository.save(pairing)
							.then(commentMongoRepository.insertPairing(comment, pairing.getId()));
				})
			)
			.flatMap(comment -> userRepository.findById(comment.getUserId())
				.switchIfEmpty(Mono.error(new UserNotFoundException()))
				.doOnSuccess(user -> {
					user.addCommentId(comment.getId());
					userRepository.save(user).subscribe();
				})
				.thenReturn(comment)
			);

		return ServerResponse.status(HttpStatus.CREATED).body(commentMono, Comment.class);
	}

	public Mono<ServerResponse> readComment(ServerRequest serverRequest) {

		String commentId = serverRequest.pathVariable("commentId");

		Mono<Comment> commentMono = commentService.findCommentByCommentId(commentId);

		return ServerResponse.ok().body(commentMono, Rating.class);
	}

	public Mono<ServerResponse> readComments() {

		Mono<List<Comment>> listMono = commentRepository.findAll()
			.sort(Comparator.comparing(Comment::getCreatedAt))
			.collectList();

		return ServerResponse.ok().body(listMono, Comment.class);
	}

	public Mono<Page<Comment>> readRatingCommentPageMono(ServerRequest serverRequest, int page) {

		String ratingId = serverRequest.pathVariable("ratingId");

		PageRequest pageRequest = PageRequest.of(page - 1, 10);

		return commentMongoRepository.findRatingCommentsPageByRatingId(ratingId, pageRequest);
	}

	public Mono<Page<Comment>> readPairingCommentPageMono(ServerRequest serverRequest, int page) {

		String pairingId = serverRequest.pathVariable("pairingId");

		PageRequest pageRequest = PageRequest.of(page - 1, 10);

		return commentMongoRepository.findPairingCommentsPageByRatingId(pairingId, pageRequest);
	}

	public Mono<ServerResponse> updateComment(ServerRequest serverRequest) {

		String commentId = serverRequest.pathVariable("commentId");

		Mono<User> currentUserMono = userService.getCurrentUser();

		Mono<Comment> commentMono = commentService.findCommentByCommentId(commentId)
			.switchIfEmpty(Mono.error(new CommentNotFoundException()));

		Mono<Comment> updateComment = Mono.zip(currentUserMono, commentMono)
			.flatMap(tuple -> {
				User currentUser = tuple.getT1();
				Comment currentComment = tuple.getT2();

				if (!currentComment.getUserId().equals(currentUser.getId())) {
					return Mono.error(new UserNotMatchException());
				}

				return serverRequest.bodyToMono(Comment.class)
					.doOnNext(this::validate)
					.flatMap(savedComment -> {
						return commentRepository.findById(commentId)
							.flatMap(comment -> {
								comment.update(savedComment);
								return commentMongoRepository.save(comment);
							})
							.thenReturn(savedComment)
							.doOnNext(comment -> commentsSink.tryEmitNext(comment));
					});

			});

		return ServerResponse.ok().body(updateComment, Comment.class);
	}

	public Mono<ServerResponse> deleteComment(ServerRequest serverRequest) {

		String commentId = serverRequest.pathVariable("commentId");

		Mono<User> currentUserMono = userService.getCurrentUser();

		Mono<Comment> commentMono = commentService.findCommentByCommentId(commentId)
			.switchIfEmpty(Mono.error(new CommentNotFoundException()));

		Mono<Void> deleteCommentMono = Mono.zip(currentUserMono, commentMono)
			.flatMap(tuple -> {
				User currentUser = tuple.getT1();
				Comment currentComment = tuple.getT2();

				if (!currentComment.getUserId().equals(currentUser.getId())) {
					return Mono.error(new UserNotMatchException());
				}

				currentUser.removeCommentId(currentComment.getId());
				userRepository.save(currentUser).subscribe();

				if (currentComment.getCommentType() == CommentType.COMMENT_TYPE_RATING) {
					return ratingRepository.findById(currentComment.getRatingId())
						.flatMap(rating -> {
							rating.removeCommentCount();
							rating.deleteCommentId(currentComment.getId());
							return ratingRepository.save(rating);
						})
						.then(commentRepository.delete(currentComment));
				} else {
					return pairingRepository.findById(currentComment.getPairingId())
						.flatMap(pairing -> {
							pairing.removeCommentCount();
							pairing.deleteCommentId(currentComment.getPairingId());
							return pairingRepository.save(pairing);
						})
						.then(commentRepository.delete(currentComment));
				}
			});

		return deleteCommentMono.then(ServerResponse.noContent().build());
	}

	/*
	 * @Valid 커스텀
	 */
	private void validate(Comment comment) {
		Errors errors = new BeanPropertyBindingResult(comment, Comment.class.getName());

		commentValidator.validate(comment, errors);
		if (errors.hasErrors()) {
			throw new ServerWebInputException(errors.toString());
		}
	}

	/*
	 * 새로고침시 내 브라우저 올클리어
	 */
	Sinks.Many<Comment> commentsSink = Sinks.many().replay().latest();

}
