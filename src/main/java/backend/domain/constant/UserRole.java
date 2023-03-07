package backend.domain.constant;

import static java.util.stream.Collectors.*;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.google.common.collect.Sets;

import lombok.Getter;

@Getter
public enum UserRole {
	ROLE_USER("USER"),
	ROLE_ADMIN("ADMIN"),
	ROLE_NONE("NONE");

	private final String role;
	private final String authority;
	private final Set<SimpleGrantedAuthority> authorities;

	private static final Map<String, UserRole> ROLE_MAP =
		Arrays.stream(UserRole.values())
			.collect(toMap(UserRole::getRole, Function.identity()));
	public static final String ROLE = "role";

	UserRole(final String role) {
		this.role = role;
		this.authority = "ROLE_" + role;
		this.authorities = Sets.newHashSet(new SimpleGrantedAuthority(this.authority));
	}

	public static UserRole lookup(final String role) {
		return Optional.ofNullable(ROLE_MAP.get(role)).orElse(ROLE_NONE);
	}
}
