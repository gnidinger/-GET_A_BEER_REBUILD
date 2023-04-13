package backend.domain.notification.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import backend.domain.notification.entity.Notification;

public interface NotificationRepository extends ReactiveMongoRepository<Notification, String> {
}
