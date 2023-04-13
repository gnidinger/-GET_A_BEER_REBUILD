package backend.domain.pairing.service;

import java.util.List;

import org.springframework.stereotype.Service;

import backend.domain.pairing.entity.Pairing;
import backend.domain.pairing.exception.PairingNotFoundException;
import backend.domain.pairing.repository.PairingMongoRepository;
import backend.domain.pairing.repository.PairingRepository;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class PairingService {
	private final PairingRepository pairingRepository;
	private final PairingMongoRepository pairingMongoRepository;

	public Mono<Pairing> findPairingByPairingId(String pairingId) {
		return pairingRepository.findById(pairingId)
			.switchIfEmpty(Mono.error(new PairingNotFoundException()));
	}

	public Mono<List<Pairing>> findRatingsByBeerId(String beerId) {
		return pairingMongoRepository.findPairingsByBeerId(beerId);
	}
}
