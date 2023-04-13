package backend.global.security.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import backend.domain.constant.UserStatus;
import backend.domain.user.entity.User;
import backend.domain.user.repository.UserRepository;
import backend.global.exception.BusinessLogicException;
import backend.global.exception.ExceptionCode;
import backend.global.security.util.CustomAuthorityUtils;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@Transactional
@RequiredArgsConstructor
@PropertySource("classpath:/application-oauth.yml")
public class KakaoService {

	@Value("${spring.security.oauth2.client.provider.kakao.token-uri}")
	private String KAKAO_TOKEN_URI;
	@Value("${spring.security.oauth2.client.registration.kakao.client-id}")
	private String KAKAO_CLIENT_ID;
	@Value("${spring.security.oauth2.client.registration.kakao.client-secret}")
	private String KAKAO_CLIENT_SECRET;
	@Value("${spring.security.oauth2.client.registration.kakao.redirect-uri}")
	private String KAKAO_REDIRECT_URI;
	@Value("${spring.security.oauth2.client.provider.kakao.user-info-uri}")
	private String KAKAO_USER_INFO_URI;

	private final UserRepository userRepository;
	private final CustomAuthorityUtils customAuthorityUtils;
	private final PasswordEncoder passwordEncoder;

	public Mono<User> doFilter(String authorizeCode) {

		String accessToken = "";
		String refreshToken = "";

		try {
			URL url = new URL(KAKAO_TOKEN_URI);
			HttpURLConnection conn = (HttpURLConnection)url.openConnection();

			//    POST 요청을 위해 기본값이 false인 setDoOutput을 true로
			conn.setRequestMethod("POST");
			conn.setDoOutput(true);

			//    POST 요청에 필요로 요구하는 파라미터 스트림을 통해 전송
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream()));
			StringBuilder sb = new StringBuilder();
			sb.append("content_type:" + "application/x-www-form-urlencoded");
			sb.append("&grant_type=authorization_code");
			sb.append("&client_id=").append(KAKAO_CLIENT_ID);
			sb.append("&client_secret=").append(KAKAO_CLIENT_SECRET);
			sb.append("&redirect_uri=").append(KAKAO_REDIRECT_URI);
			sb.append("&client_name=Kakao");
			sb.append("&code=").append(authorizeCode);
			bw.write(sb.toString());
			bw.flush();

			// 결과 코드가 200이면 성공
			int responseCode = conn.getResponseCode();
			System.out.println("responseCode : " + responseCode);

			// 요청을 통해 얻은 JSON타입의 Response 메세지 읽어오기
			BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			StringBuilder result = new StringBuilder();
			String line = "";

			while ((line = br.readLine()) != null) {
				result.append(line);
			}
			System.out.println("response body : " + result);

			// Gson 라이브러리에 포함된 클래스로 JSON파싱 객체 생성
			JsonElement element = JsonParser.parseString(result.toString());

			accessToken = element.getAsJsonObject().get("access_token").getAsString();
			refreshToken = element.getAsJsonObject().get("refresh_token").getAsString();

			System.out.println("access_token : " + accessToken);
			System.out.println("refresh_token : " + refreshToken);

			br.close();
			bw.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return getUserInfo(accessToken);
		// return accessToken;
	}

	public Mono<User> getUserInfo(String accessToken) {

		// 요청하는 클라이언트마다 가진 정보가 다를 수 있기에 HashMap타입으로 선언
		HashMap<String, Object> userInfo = new HashMap<>();

		try {
			URL url = new URL(KAKAO_USER_INFO_URI);
			HttpURLConnection conn = (HttpURLConnection)url.openConnection();
			conn.setRequestMethod("POST");

			// 요청에 필요한 Header에 포함될 내용
			conn.setRequestProperty("Authorization", "Bearer " + accessToken);

			int responseCode = conn.getResponseCode();
			System.out.println("responseCode : " + responseCode);

			BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));

			StringBuilder result = new StringBuilder();
			String line = "";

			while ((line = br.readLine()) != null) {
				result.append(line);
			}

			JsonElement element = JsonParser.parseString(result.toString());

			JsonObject properties = element.getAsJsonObject().get("properties").getAsJsonObject();
			JsonObject kakao_account = element.getAsJsonObject().get("kakao_account").getAsJsonObject();

			String providerId = element.getAsJsonObject().get("id").getAsString();
			String nickname = properties.getAsJsonObject().get("nickname").getAsString();
			String picture = properties.getAsJsonObject().get("profile_image").getAsString();
			String email = null;
			if (properties.getAsJsonObject().get("nickname") != null) {
				email = kakao_account.getAsJsonObject().get("email").getAsString();
			}

			userInfo.put("providerId", providerId);
			userInfo.put("nickname", nickname);
			userInfo.put("profile_image", picture);
			userInfo.put("email", email);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return createOrReturnUser(userInfo);
		// return userInfo;
	}

	public Mono<User> createOrReturnUser(HashMap<String, Object> userInfo) { // OAuth 인증이 끝나 유저 정보를 받은 경우

		String providerId = userInfo.get("providerId").toString();
		String email;
		if (userInfo.get("email") != null) {
			email = userInfo.get("email").toString();
		} else {
			email = "noemailuser@email.com";
		}
		String picture = userInfo.get("profile_image").toString();
		String nickname = userInfo.get("nickname").toString();
		String encodedPass = passwordEncoder.encode(userInfo.get("nickname").toString());

		Mono<User> findUserMono = userRepository.findByProviderId(providerId);

		return findUserMono.flatMap(findUser -> {
			if (findUser != null && findUser.getProvider() != null && findUser.getProvider().equals("KAKAO")) {
				return Mono.just(findUser);
			} else {

				Mono<User> emailUserMono = userRepository.findByEmail(email);

				return emailUserMono.flatMap(emailUser -> {
					if (emailUser != null) {
						throw new BusinessLogicException(ExceptionCode.EMAIL_USED_ANOTHER_ACCOUNT);
					} else {

						Mono<User> nicknameUserMono = userRepository.findByNickname(nickname);

						return nicknameUserMono.flatMap(nicknameUser -> {
							User.UserBuilder userBuilder = User.builder();
							userBuilder.email(email);

							if (nicknameUser == null) {
								userBuilder.nickname(nickname);
							} else {
								String rand = UUID.randomUUID().toString().substring(0, 6);
								userBuilder.nickname(nickname + "_KAKAO_" + rand);
							}

							userBuilder.status(UserStatus.ACTIVE_USER.getStatus());
							userBuilder.profileImage(picture);
							userBuilder.roles(customAuthorityUtils.createRoles(email));
							userBuilder.password(encodedPass);
							userBuilder.provider("KAKAO");
							userBuilder.providerId(providerId);

							return userRepository.save(userBuilder.build());
						});
					}
				});
			}
		});
	}
}
