package backend.domain.user.service;

import java.security.Principal;

import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Service;

import backend.domain.user.dto.UserRequestDto;
import backend.domain.user.dto.UserResponseDto;
import backend.domain.user.entity.User;
import backend.domain.user.exception.UserNotFoundException;
import backend.domain.user.repository.UserMongoRepository;
import backend.domain.user.repository.UserRepository;
import backend.global.exception.BusinessLogicException;
import backend.global.exception.ExceptionCode;
import lombok.RequiredArgsConstructor;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class UserService {
	private final UserRepository userRepository;
	private final UserMongoRepository userMongoRepository;

	public Mono<UserResponseDto.FirstSigninResponse> firstSignin(
		UserRequestDto.FirstSigninRequest firstSigninRequest, User signinUser) {

		return userRepository.findById(signinUser.getId())
			.map(user -> user.firstSignin(firstSigninRequest))
			.flatMap(userRepository::save)
			.map(user -> UserResponseDto.FirstSigninResponse.builder()
				.id(user.getId())
				.email(user.getEmail())
				.nickname(user.getNickname())
				.profileImage(user.getProfileImage())
				.genderType(user.getGenderType())
				.ageType(user.getAgeType())
				.favoriteBeerCategoryList(user.getFavoriteBeerCategoryList())
				.favoriteBeerTagList(user.getFavoriteBeerTagList())
				.build());

	}

	public Mono<User> findUserByUserId(String userId) {

		return userRepository.findById(userId)
			.map(user -> User.builder()
				.id(user.getId())
				.email(user.getEmail())
				.nickname(user.getNickname())
				.profileImage(user.getProfileImage())
				.roles(user.getRoles())
				.genderType(user.getGenderType())
				.ageType(user.getAgeType())
				.favoriteBeerCategoryList(user.getFavoriteBeerCategoryList())
				.favoriteBeerTagList(user.getFavoriteBeerTagList())
				.ratingList(user.getRatingList())
				.pairingList(user.getPairingList())
				.commentList(user.getCommentList())
				.likeList(user.getLikeList())
				// .authProvider(user.getAuthProvider())
				.build())
			.switchIfEmpty(Mono.error(new UserNotFoundException()));
	}

	public Mono<UserResponseDto.UpdateInfoResponse> updateUserInfo(
		UserRequestDto.UpdateInfoRequest updateInfoRequest, User signinUser) {

		return userRepository.findById(signinUser.getId())
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

	public Mono<User> getCurrentUser() {
		return ReactiveSecurityContextHolder.getContext()
			.map(SecurityContext::getAuthentication)
			.map(Principal::getName)
			.switchIfEmpty(Mono.error(new UserNotFoundException()))
			.flatMap(userRepository::findByEmail);
	}

	public Disposable findUserByNickname(String nickname) {
		return userRepository.findByNickname(nickname)
			.flatMap(user -> {
				if (user.getId() != null) {
					return Mono.error(new BusinessLogicException(ExceptionCode.NICKNAME_EXIST));
				} else {
					return Mono.empty();
				}
			}).subscribe();
	}

	public Disposable findUserByEmail(String email) {
		return userRepository.findByEmail(email)
			.flatMap(user -> {
				if (user.getId() != null) {
					return Mono.error(new BusinessLogicException(ExceptionCode.EMAIL_EXIST));
				} else {
					return Mono.empty();
				}
			}).subscribe();
	}
}
