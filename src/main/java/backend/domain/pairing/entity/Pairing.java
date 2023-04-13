package backend.domain.pairing.entity;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@Builder
@ToString
@Document(collection = "pairing")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Pairing {

	@Id
	private String id;
	private String beerId;
	private String userId;
	private String content;
	@Builder.Default
	private Integer likeCount = 0;
	@Builder.Default
	private Integer commentCount = 0;
	private String pairingCategory;
	private String pairingImagePath;
	private List<String> commentList;
	@CreatedDate
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime createdAt = LocalDateTime.now();
	@LastModifiedDate
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime modifiedAt;

	public void addBeerId(String beerId) {
		this.beerId = beerId;
	}

	public void update(Pairing pairing) {

		if (pairing.getContent() != null) {
			this.content = pairing.getContent();
		}
		if (pairing.getPairingCategory() != null) {
			this.pairingCategory = pairing.getPairingCategory();
		}
		this.modifiedAt = LocalDateTime.now();
	}

	public void addCommentCount() {
		this.commentCount++;
	}

	public void removeCommentCount() {
		if (this.commentCount > 0) {
			this.commentCount--;
		}
	}
	public void addUserId(String userId) {
		this.userId = userId;
	}

	public void deleteCommentId(String commentId) {
		this.commentList.remove(commentId);
	}

	public void addLikeCount() {
		this.likeCount++;
	}

	public void removeLikeCount() {
		if (this.likeCount > 0) {
			this.likeCount--;
		}
	}
}
