package backend.domain.beer.entity;

import static backend.domain.constant.Constant.*;

import java.util.List;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import backend.domain.pairing.entity.Pairing;
import backend.domain.rating.entity.Rating;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@Builder
@ToString
@Document(collection = "beer")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Beer {

	@Id
	private String id;
	private String beerImagePath;
	private String korName;
	private String country;
	@Builder.Default
	private Double totalAverageStar = 0.0;
	@Builder.Default
	private Long totalStarCount = 0L;
	@Builder.Default
	private Double femaleAverageStar = 0.0;
	@Builder.Default
	private Long femaleStarCount = 0L;
	@Builder.Default
	private Double maleAverageStar = 0.0;
	@Builder.Default
	private Long maleStarCount = 0L;
	@Builder.Default
	private Double othersAverageStar = 0.0;
	@Builder.Default
	private Long othersStarCount = 0L;
	@Builder.Default
	private Double abv = 0.0;
	@Builder.Default
	private Integer ibu = 0;
	private List<String> beerCategoryList;
	@Builder.Default
	private Map<String, Integer> beerTagMap = BeerTagMap();
	@Builder.Default
	private Map<String, Integer> pairingCategoryMap = PairingCategoryMap();
	private List<String> ratingList;
	private List<String> pairingList;

	public void update(Beer beer) {
		if (beer.getKorName() != null) {
			this.korName = beer.getKorName();
		}
		if (beer.getCountry() != null) {
			this.country = beer.getCountry();
		}
		if (beer.getAbv() != null) {
			this.abv = beer.getAbv();
		}
	}

	public void addTotalAverageStar(Double star) {
		Double currentAverageStar = this.totalAverageStar * this.totalStarCount;
		this.totalStarCount++;
		this.totalAverageStar = (currentAverageStar + star) / totalStarCount;
	}

	public void addFemaleAverageStar(Double star) {
		Double currentAverageStar = this.femaleAverageStar * this.femaleStarCount;
		this.femaleStarCount++;
		this.femaleAverageStar = (currentAverageStar + star) / femaleStarCount;
	}

	public void addMaleAverageStar(Double star) {
		Double currentAverageStar = this.maleAverageStar * this.maleStarCount;
		this.maleStarCount++;
		this.maleAverageStar = (currentAverageStar + star) / maleStarCount;
	}

	public void addOthersAverageStar(Double star) {
		Double currentAverageStar = this.othersAverageStar * this.othersStarCount;
		this.othersStarCount++;
		this.othersAverageStar = (currentAverageStar + star) / othersStarCount;
	}

	public void removeTotalAverageStar(Double star) {
		Double currentAverageStar = this.totalAverageStar * this.totalStarCount;
		if (totalStarCount > 0) {
			this.totalStarCount--;
			if (currentAverageStar - star == 0) {
				this.totalAverageStar = 0.0;
			} else {
				this.totalAverageStar = (currentAverageStar - star) / totalStarCount;
			}
		}
	}

	public void removeFemaleAverageStar(Double star) {
		Double currentAverageStar = this.femaleAverageStar * this.femaleStarCount;
		if (femaleStarCount > 0) {
			this.femaleStarCount--;
			if (currentAverageStar - star == 0) {
				this.femaleAverageStar = 0.0;
			} else {
				this.femaleAverageStar = (currentAverageStar - star) / femaleStarCount;
			}
		}
	}

	public void removeMaleAverageStar(Double star) {
		Double currentAverageStar = this.maleAverageStar * this.maleStarCount;
		if (maleStarCount > 0) {
			this.maleStarCount--;
			if (currentAverageStar - star == 0) {
				this.maleAverageStar = 0.0;
			} else {
				this.maleAverageStar = (currentAverageStar - star) / maleStarCount;
			}
		}
	}

	public void removeOthersAverageStar(Double star) {
		Double currentAverageStar = this.othersAverageStar * this.othersStarCount;
		if (othersStarCount > 0) {
			this.othersStarCount--;
			if (currentAverageStar - star == 0) {
				this.othersAverageStar = 0.0;
			} else {
				this.othersAverageStar = (currentAverageStar - star) / othersStarCount;
			}
		}
	}

	public Beer updateBeerTag(Rating rating) {
		for (String beerTag : rating.getBeerTagList()) {
			if (this.beerTagMap.containsKey(beerTag)) {
				this.beerTagMap.replace(beerTag, this.beerTagMap.get(beerTag) + 1);
			}
		}
		return this;
	}

	public Beer deleteBeerTag(Rating rating) {
		for (String beerTag : rating.getBeerTagList()) {
			if (this.beerTagMap.get(beerTag) > 0) {
				this.beerTagMap.replace(beerTag, this.beerTagMap.get(beerTag) - 1);
			}
		}
		return this;
	}

	public Beer updatePairingCategory(Pairing pairing) {
		String pairingCategory = pairing.getPairingCategory();
		if (this.pairingCategoryMap.containsKey(pairingCategory)) {
			this.pairingCategoryMap.replace(pairingCategory, this.pairingCategoryMap.get(pairingCategory) + 1);
		}
		return this;
	}

	public Beer deletePairingCategory(Pairing pairing) {
		String pairingCategory = pairing.getPairingCategory();
		this.pairingCategoryMap.replace(pairingCategory, this.pairingCategoryMap.get(pairingCategory) - 1);
		return this;
	}

	public void deleteRatingId(String ratingId) {
		this.ratingList.remove(ratingId);
	}

	public void deletePairingId(String pairingId) {
		this.pairingList.remove(pairingId);
	}

}
