package backend.domain.user.service;

import org.springframework.stereotype.Service;

import backend.domain.user.dto.UserRequestDto;
import backend.domain.user.dto.UserResponseDto;
import backend.domain.user.entity.User;
import backend.domain.user.exception.UserNotFoundException;
import backend.domain.user.repository.UserMongoRepository;
import backend.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class UserService {
	private final UserRepository userRepository;
	private final UserMongoRepository userMongoRepository;

	public Mono<User> findUserByUserId(String userId) {
		return userRepository.findById(userId)
			.map(user -> User.builder()
				.id(user.getId())
				.email(user.getEmail())
				.nickname(user.getNickname())
				.profileImage(user.getProfileImage())
				.genderType(user.getGenderType())
				.ageType(user.getAgeType())
				.favoriteBeerCategoryList(user.getFavoriteBeerCategoryList())
				.favoriteBeerTagList(user.getFavoriteBeerTagList())
				// .authProvider(user.getAuthProvider())
				.build())
			.switchIfEmpty(Mono.error(new UserNotFoundException()));
	}

	public Mono<UserResponseDto.UpdateInfoResponse> updateUserInfo(
		UserRequestDto.UpdateInfoRequest updateInfoRequest, User signInUser) {

		return userRepository.findById(signInUser.getId())
			.map(user -> user.update(updateInfoRequest))
			.flatMap(userRepository::save)
			.map(user -> UserResponseDto.UpdateInfoResponse.builder()
				.id(user.getId())
				.email(user.getEmail())
				.nickname(user.getNickname())
				.profileImage(user.getProfileImage())
				.genderType(user.getGenderType())
				.ageType(user.getAgeType())
				.favoriteBeerCategoryList(user.getFavoriteBeerCategoryList())
				.favoriteBeerTagList(user.getFavoriteBeerTagList())
				// .authProvider(user.getAuthProvider())
				.build());
	}

}
