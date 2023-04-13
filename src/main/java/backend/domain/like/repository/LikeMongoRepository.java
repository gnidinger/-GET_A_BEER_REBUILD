package backend.domain.like.repository;

import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LikeMongoRepository {

	private final ReactiveMongoTemplate reactiveMongoTemplate;
}
