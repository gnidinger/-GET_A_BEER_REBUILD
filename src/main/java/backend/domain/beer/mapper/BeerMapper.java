package backend.domain.beer.mapper;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;

import backend.domain.beer.dto.BeerResponseDto;
import backend.domain.beer.entity.Beer;
import backend.domain.pairing.entity.Pairing;
import backend.domain.rating.entity.Rating;

@Mapper(componentModel = "spring")
public interface BeerMapper {

	default BeerResponseDto.ReadResponse beerToReadResponse(Beer beer, List<Rating> ratingList,
		List<Pairing> pairingList) {

		BeerResponseDto.ReadResponse.ReadResponseBuilder readResponseBuilder = BeerResponseDto.ReadResponse.builder();

		readResponseBuilder.id(beer.getId());
		readResponseBuilder.beerImagePath(beer.getBeerImagePath());
		readResponseBuilder.korName(beer.getKorName());
		readResponseBuilder.country(beer.getCountry());
		readResponseBuilder.abv(beer.getAbv());
		readResponseBuilder.ibu(beer.getIbu());
		readResponseBuilder.beerCategoryList(beer.getBeerCategoryList());
		// readResponseBuilder.beerTagMap(beer.getBeerTagMap());
		readResponseBuilder.beerCategoryList(beer.getBeerCategoryList());
		// readResponseBuilder.pairingCategoryMap(beer.getPairingCategoryMap());
		// readResponseBuilder.ratingList(ratingList);
		// readResponseBuilder.pairingList(pairingList);

		return readResponseBuilder.build();

	}

	default BeerResponseDto.ReadResponse beerToReadResponseTemp(Beer beer) {

		BeerResponseDto.ReadResponse.ReadResponseBuilder readResponseBuilder = BeerResponseDto.ReadResponse.builder();

		readResponseBuilder.id(beer.getId());
		readResponseBuilder.beerImagePath(beer.getBeerImagePath());
		readResponseBuilder.korName(beer.getKorName());
		readResponseBuilder.country(beer.getCountry());
		readResponseBuilder.totalAverageStar(round(beer.getTotalAverageStar()));
		readResponseBuilder.totalStarCount(beer.getTotalStarCount());
		readResponseBuilder.femaleAverageStar(round(beer.getFemaleAverageStar()));
		readResponseBuilder.femaleStarCount(beer.getFemaleStarCount());
		readResponseBuilder.maleAverageStar(round(beer.getMaleAverageStar()));
		readResponseBuilder.maleStarCount(beer.getMaleStarCount());
		readResponseBuilder.othersAverageStar(round(beer.getOthersAverageStar()));
		readResponseBuilder.othersStarCount(beer.getOthersStarCount());
		readResponseBuilder.abv(beer.getAbv());
		readResponseBuilder.ibu(beer.getIbu());
		readResponseBuilder.beerCategoryList(beer.getBeerCategoryList());
		readResponseBuilder.topTagList(beer.getBeerTagMap().entrySet().stream()
			.sorted((o1, o2) -> o2.getValue() - o1.getValue())
			.map(Map.Entry::getKey)
			.map(Object::toString).limit(4)
			.collect(Collectors.toList()));
		// readResponseBuilder.beerTagMap(beer.getBeerTagMap());
		readResponseBuilder.beerCategoryList(beer.getBeerCategoryList());
		readResponseBuilder.topPairingCategory(beer.getPairingCategoryMap().entrySet().stream()
			.sorted((o1, o2) -> o2.getValue() - o1.getValue())
			.map(Map.Entry::getKey)
			.map(Objects::toString)
			.findFirst()
			.get());
		// readResponseBuilder.pairingCategoryMap(beer.getPairingCategoryMap());
		readResponseBuilder.ratingList(beer.getRatingList());
		readResponseBuilder.pairingList(beer.getPairingList());

		return readResponseBuilder.build();
	}

	private Double round(Double average) {
		return Math.round(average * 100) / 100.0;
	}
}
