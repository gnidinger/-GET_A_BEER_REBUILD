package backend.domain.comment.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import backend.domain.comment.entity.Comment;

public interface CommentRepository extends ReactiveMongoRepository<Comment, String> {
}
