package backend.domain.comment.service;

import org.springframework.stereotype.Service;

import backend.domain.comment.entity.Comment;
import backend.domain.comment.exception.CommentNotFoundException;
import backend.domain.comment.repository.CommentRepository;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class CommentService {
	private final CommentRepository commentRepository;

	public Mono<Comment> findCommentByCommentId(String commentId) {
		return commentRepository.findById(commentId)
			.switchIfEmpty(Mono.error(new CommentNotFoundException()));
	}
}
