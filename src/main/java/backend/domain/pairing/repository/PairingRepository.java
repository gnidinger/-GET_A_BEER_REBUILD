package backend.domain.pairing.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import backend.domain.pairing.entity.Pairing;

public interface PairingRepository extends ReactiveMongoRepository<Pairing, String> {
}
