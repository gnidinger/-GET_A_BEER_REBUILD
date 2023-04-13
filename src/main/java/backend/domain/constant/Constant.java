package backend.domain.constant;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Constant {
	public static final Set<String> BEER_CATEGORY_SET =
		new HashSet<>(List.of(
			"ALE", "IPA", "LAGER", "WEIZEN", "WEISSBIER", "HEFEWEIZEN", "GOSE",
			"DUNKEL", "STOUT", "PILSENER", "LAMBIC", "FRUIT", "NON_ALCOHOLIC", "ETC"));

	public static final Set<String> BEER_TAG_SET =
		new LinkedHashSet<>(List.of(
			"STRAW", "GOLD", "BROWN", "BLACK", // COLOR
			"SWEET", "SOUR", "BITTER", "ROUGH",  // TASTE
			"FRUITY", "FLOWER", "MALTY", "NO_SCENT", // FLAVOR
			"WEAK", "MIDDLE", "STRONG", "NO_CARBONATION" // CARBONATION
		));

	public static final Set<String> PAIRING_CATEGORY_SET =
		new LinkedHashSet<>(List.of(
			"FRIED", "GRILL", "STIR", "FRESH",
			"DRY", "SNACK", "SOUP", "ETC"
		));

	public static final Set<String> GENDER_TYPE_SET =
		new LinkedHashSet<>(List.of(
			"MALE", "FEMALE", "OTHERS", "NONE"
		));

	public static final Set<String> AGE_TYPE_SET =
		new LinkedHashSet<>(List.of(
			"TEENAGER", "TWENTIES", "THIRTIES", "FORTIES",
			"FIFTY", "OVER", "OTHERS", "NONE"
		));

	public static final Set<String> COMMENT_TYPE =
		new HashSet<>(List.of(
			"RATING_COMMENT", "PAIRING_COMMENT"
		));

	public static Map<String, Integer> BeerTagMap() {
		Map<String, Integer> map = new LinkedHashMap<>();
		BEER_TAG_SET.forEach(beerTag -> map.put(beerTag, 0));
		return map;
	}

	public static Map<String, Integer> PairingCategoryMap() {
		Map<String, Integer> map = new LinkedHashMap<>();
		PAIRING_CATEGORY_SET.forEach(pairingCategory -> map.put(pairingCategory, 0));
		return map;
	}
}
