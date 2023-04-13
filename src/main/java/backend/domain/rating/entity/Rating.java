package backend.domain.rating.entity;

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
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@ToString
@Document(collection = "rating")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Rating {

	@Id
	private String id;
	private String beerId;
	private String userId;
	private Double star;
	private String content;
	@Builder.Default
	private Integer likeCount = 0;
	@Builder.Default
	private Integer commentCount = 0;
	private List<String> beerTagList;
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

	public void update(Rating rating, String ratingId) {
		// this.id = ratingId;
		if (rating.getStar() != null) {
			this.star = rating.getStar();
		}
		if (rating.getContent() != null) {
			this.content = rating.getContent();
		}
		if (rating.getBeerTagList() != null) {
			this.beerTagList = rating.getBeerTagList();
		}
		this.modifiedAt = LocalDateTime.now();
	}

	public void deleteCommentId(String commentId) {
		this.commentList.remove(commentId);
	}

	// public Beer toBeer(Beer beer) {
	// 	beer.getRatingList().add(this);
	// 	return beer;
	// }

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

	public void addLikeCount() {
		this.likeCount++;
	}

	public void removeLikeCount() {
		if (this.likeCount > 0) {
			this.likeCount--;
		}
	}
}
