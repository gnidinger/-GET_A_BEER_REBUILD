package backend.domain.user.test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Service;

import backend.global.security.entity.AuthUser;
import backend.global.security.entity.UserRole;
import reactor.core.publisher.Mono;

@Service
public class TestService {

	private Map<String, AuthUser> data;

	@PostConstruct
	public void init() {
		data = new HashMap<>();

		//username:passwowrd -> user:user
		data.put("a@mail.com", AuthUser.builder()
			.email("a@mail.com")
			.password("12341234")
			.nickname("거니맘")
			.roles(List.of(UserRole.ROLE_USER))
			.build());

		//username:passwowrd -> admin:admin
		// data.put("admin", new AuthUser("admin", "dQNjUIMorJb8Ubj2+wVGYp6eAeYkdekqAcnYp+aRq5w=", true, Arrays.asList(
		// 	UserRole.ROLE_ADMIN)));
	}

	public Mono<AuthUser> findByUsername(String email) {
		return Mono.justOrEmpty(data.get(email));
	}
}
