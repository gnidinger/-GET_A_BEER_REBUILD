package backend.domain.user.dto;

import java.util.List;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class UserResponseDto {


	@Getter
	@Builder
	@NoArgsConstructor(access = AccessLevel.PROTECTED)
	@AllArgsConstructor(access = AccessLevel.PROTECTED)
	public static class FirstSigninResponse {

		private String id;
		private String email;
		private String nickname;
		private String profileImage;
		private String genderType;
		private String ageType;
		private List<String> favoriteBeerCategoryList;
		private List<String> favoriteBeerTagList;
	}

	@Getter
	@Builder
	@NoArgsConstructor(access = AccessLevel.PROTECTED)
	@AllArgsConstructor(access = AccessLevel.PROTECTED)
	public static class UpdateInfoResponse {

		private String id;
		private String email;
		private String nickname;
		private String profileImage;
		private String genderType;
		private String ageType;
		private List<String> favoriteBeerCategoryList;
		private List<String> favoriteBeerTagList;
	}

	@Getter
	@Builder
	@NoArgsConstructor(access = AccessLevel.PROTECTED)
	@AllArgsConstructor(access = AccessLevel.PROTECTED)
	public static class PrincipalResponse {

		private String id;
		private String email;
		private String nickname;
	}
}
