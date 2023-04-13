package backend.domain.beer.dto;

import java.util.List;
import java.util.Map;

import backend.domain.pairing.entity.Pairing;
import backend.domain.rating.entity.Rating;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class BeerResponseDto {

	@Getter
	@Builder
	@NoArgsConstructor(access = AccessLevel.PROTECTED)
	@AllArgsConstructor(access = AccessLevel.PROTECTED)
	public static class ReadResponse {

		private String id;
		private String beerImagePath;
		private String korName;
		private String country;
		private Double totalAverageStar;
		private Long totalStarCount;
		private Double femaleAverageStar;
		private Long femaleStarCount;
		private Double maleAverageStar;
		private Long maleStarCount;
		private Double othersAverageStar;
		private Long othersStarCount;
		private Double abv;
		private Integer ibu;
		private List<String> beerCategoryList;
		private List<String> topTagList;
		private String topPairingCategory;
		// private Map<String, Integer> beerTagMap;
		// private Map<String, Integer> pairingCategoryMap;
		private List<String> ratingList;
		private List<String> pairingList;

	}
}
