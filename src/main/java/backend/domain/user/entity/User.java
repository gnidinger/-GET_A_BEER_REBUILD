package backend.domain.user.entity;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import backend.domain.user.dto.UserRequestDto;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
@Getter
@Builder
@ToString
@Document(collection = "user")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

	@Id
	private String id;
	private String email;
	private String nickname;
	private String password;
	private String profileImage;
	private String genderType;
	private String ageType;
	private List<String> favoriteBeerCategoryList;
	private List<String> favoriteBeerTagList;
	private List<String> roles = new ArrayList<>();

	public User firstSignin(UserRequestDto.FirstSignIn firstSignIn) {

		this.genderType = firstSignIn.getGenderType();
		this.ageType = firstSignIn.getAgeType();
		this.favoriteBeerCategoryList = firstSignIn.getFavoriteBeerCategoryList();
		this.favoriteBeerTagList = firstSignIn.getFavoriteBeerTagList();

		return this;
	}

	public User update(final UserRequestDto.UpdateInfoRequest updateInfoRequest) {

		if (updateInfoRequest.getProfileImage() != null) {
			if (updateInfoRequest.getProfileImage().startsWith("http")) {
				this.profileImage = updateInfoRequest.getProfileImage();
			} else {
				this.profileImage = "";
			}
		}
		if (updateInfoRequest.getNickname() != null) {
			this.nickname = updateInfoRequest.getNickname();
		}

		if (updateInfoRequest.getGenderType() != null) {
			this.genderType = updateInfoRequest.getGenderType();
		}

		if (updateInfoRequest.getAgeType() != null) {
			this.ageType = updateInfoRequest.getAgeType();
		}

		if (updateInfoRequest.getFavoriteBeerCategoryList() != null) {
			this.favoriteBeerCategoryList = updateInfoRequest.getFavoriteBeerCategoryList();
		}

		if (updateInfoRequest.getFavoriteBeerTagList() != null) {
			this.favoriteBeerTagList = updateInfoRequest.getFavoriteBeerTagList();
		}

		return this;
	}
}
