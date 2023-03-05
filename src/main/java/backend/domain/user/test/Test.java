package backend.domain.user.test;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import backend.global.security.config.PBKDF2Encoder;
import backend.global.security.dto.SigninDto;
import backend.global.security.jwt.JwtTokenizer;
import lombok.AllArgsConstructor;
import reactor.core.publisher.Mono;

@RestController
@AllArgsConstructor
public class Test {

	private JwtTokenizer jwtTokenizer;
	private PBKDF2Encoder passwordEncoder;
	private TestService userService;

	@PostMapping("/login")
	public Mono<ResponseEntity<SigninDto.Response>> login(@RequestBody SigninDto.Request ar) {
		return userService.findByUsername(ar.getEmail())
			.filter(userDerails -> ar.getPassword().equals(userDerails.getPassword()))
			// .filter(userDetails -> passwordEncoder.encode(ar.getPassword()).equals(userDetails.getPassword()))
			.map(userDetails -> ResponseEntity.ok(new SigninDto.Response(jwtTokenizer.generateToken(userDetails))))
			.switchIfEmpty(Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()));
	}
}
