package backend.domain.user.dto;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;

import backend.domain.user.entity.User;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
public class UserRequestDto {

	@Getter
	@Builder
	@NoArgsConstructor(access = AccessLevel.PROTECTED)
	@AllArgsConstructor(access = AccessLevel.PROTECTED)
	public static class SignUp {

		private String email;
		private String nickname;
		private String password;

		public User toUser(PasswordEncoder passwordEncoder) {
			return User.builder()
				.email(this.email)
				.nickname(this.nickname)
				.roles(List.of("ROLE_USER"))
				.password(passwordEncoder.encode(this.password))
				.build();
		}
	}

	@Getter
	@Builder
	@NoArgsConstructor(access = AccessLevel.PROTECTED)
	@AllArgsConstructor(access = AccessLevel.PROTECTED)
	public static class FirstSigninRequest {

		private String genderType;
		private String ageType;
		private List<String> favoriteBeerCategoryList;
		private List<String> favoriteBeerTagList;
	}

	@Getter
	@Builder
	@NoArgsConstructor(access = AccessLevel.PROTECTED)
	@AllArgsConstructor(access = AccessLevel.PROTECTED)
	public static class UpdateInfoRequest {

		private String profileImage;
		private String nickname;
		private String genderType;
		private String ageType;
		private List<String> favoriteBeerCategoryList;
		private List<String> favoriteBeerTagList;
	}

}
