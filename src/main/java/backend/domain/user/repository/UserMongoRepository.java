package backend.domain.user.repository;

import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserMongoRepository {
	private final ReactiveMongoTemplate reactiveMongoTemplate;

}
