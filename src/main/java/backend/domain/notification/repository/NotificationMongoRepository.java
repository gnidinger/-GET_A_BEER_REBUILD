package backend.domain.notification.repository;

import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationMongoRepository {

	private final ReactiveMongoTemplate reactiveMongoTemplate;
}
