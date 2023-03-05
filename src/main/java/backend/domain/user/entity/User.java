package backend.domain.user.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

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
public class User implements Serializable {

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

}
