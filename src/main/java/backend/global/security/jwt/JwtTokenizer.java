package backend.global.security.jwt;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import backend.domain.user.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtTokenizer {

	@Value("${jwt.secret-key}")
	private String secret;
	@Value("${jwt.access-token-expiration-minutes}")
	private String accessTokenExpirationTime;
	@Value("${jwt.refresh-token-expiration-minutes}")
	private String refreshTokenExpirationMinutes;
	private Key key;

	@PostConstruct
	public void init() {
		this.key = Keys.hmacShaKeyFor(secret.getBytes());
	}

	public String delegateAccessToken(User user) {
		Map<String, Object> claims = new HashMap<>();
		claims.put("email", user.getEmail());
		claims.put("role", user.getRoles());
		return generateAccessToken(claims, user.getEmail());
	}

	public String delegateRefreshToken(User user) {
		String subject = user.getEmail();
		return generateRefreshToken(subject);
	}

	private String generateAccessToken(Map<String, Object> claims, String subject) {

		Long expirationTimeLong = Long.parseLong(accessTokenExpirationTime); //in second
		final Date createdDate = new Date();
		final Date expirationDate = new Date(createdDate.getTime() + expirationTimeLong * 1000);

		return Jwts.builder()
			.setClaims(claims)
			.setSubject(subject)
			.setIssuedAt(createdDate)
			.setExpiration(expirationDate)
			.signWith(key)
			.compact();
	}

	private String generateRefreshToken(String subject) {

		Long expirationTimeLong = Long.parseLong(refreshTokenExpirationMinutes); //in second
		final Date createdDate = new Date();
		final Date expirationDate = new Date(createdDate.getTime() + expirationTimeLong * 1000);

		return Jwts.builder()
			.setSubject(subject)
			.setIssuedAt(createdDate)
			.setExpiration(expirationDate)
			.signWith(key)
			.compact();
	}

	public Boolean validateToken(String token) {
		return !isTokenExpired(token);
	}

	private Boolean isTokenExpired(String token) {
		final Date expiration = getExpirationDateFromToken(token);
		return expiration.before(new Date());
	}

	public Date getExpirationDateFromToken(String token) {
		return getAllClaimsFromToken(token).getExpiration();
	}

	public Claims getAllClaimsFromToken(String token) {
		return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
	}

	public String getUsernameFromToken(String token) {
		return getAllClaimsFromToken(token).getSubject();
	}

}
