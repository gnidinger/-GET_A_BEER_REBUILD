package backend.domain.comment.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonFormat;

import backend.domain.constant.CommentType;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@Builder
@ToString
@Document(collection = "comment")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment {

	@Id
	private String id;
	private CommentType commentType;
	private String ratingId;
	private String pairingId;
	private String userId;
	private String content;
	@Builder.Default
	private Integer likeCount = 0;
	@CreatedDate
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime createdAt = LocalDateTime.now();
	@LastModifiedDate
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime modifiedAt;

	public void addUserId(String userId) {
		this.userId = userId;
	}

	public void addRatingId(String ratingId) {
		this.ratingId = ratingId;
	}

	public void addPairingId(String pairingId) {
		this.pairingId = pairingId;
	}

	public void addCommentType(CommentType commentType) {
		this.commentType = commentType;
	}

	public void update(Comment comment) {
		if (comment.getContent() != null) {
			this.content = comment.getContent();
		}
		this.modifiedAt = LocalDateTime.now();
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
