package jp.brainjuice.pokego.business.service;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ibm.icu.text.Transliterator;

import jp.brainjuice.pokego.business.dao.GoPokedexRepository;
import jp.brainjuice.pokego.business.dao.entity.GoPokedex;
import jp.brainjuice.pokego.business.service.utils.PokemonGoUtils;
import jp.brainjuice.pokego.business.service.utils.dto.PokemonSearchResult;

@Service
public class PokemonSearchService {

	private GoPokedexRepository goPokedexRepository;

	private PokemonGoUtils pokemonGoUtils;

	/** ひらカタ漢字は全角に、ＡＢＣ１２３は半角に */
	private Transliterator transAnyNFKC = Transliterator.getInstance("Any-NFKC");

	/** ひらがな→カタカナ */
	private Transliterator transHiraToKana = Transliterator.getInstance("Hiragana-Katakana");

	private static final String MSG_RESULTS = "{0}件のポケモンがヒットしました！";

	private static final String MSG_NO_RESULTS = "該当するポケモンがいませんでした。";

	@Autowired
	public PokemonSearchService(
			GoPokedexRepository goPokedexRepository,
			PokemonGoUtils pokemonGoUtils) {
		this.goPokedexRepository = goPokedexRepository;
		this.pokemonGoUtils = pokemonGoUtils;
	}

	public PokemonSearchResult search(String name) {

		PokemonSearchResult result = new PokemonSearchResult();

		String transName;
		transName = transAnyNFKC.transliterate(name);
		transName = transHiraToKana.transliterate(transName);

		// 検索
		List<GoPokedex> goPokedexList = goPokedexRepository.findByNameIn(Arrays.asList(transName, "メガ" + transName));

		// 1件もヒットしなかった場合
		if (goPokedexList.isEmpty()) {

			if (transName.length() <= 20) {
				// すごく曖昧に検索する。
				goPokedexList = searchFuzzy(transName);
				result.setMaybe(true);
			} else {
				// なんか負荷がかかりそうだから文字数が多いときは検索させない。
				result.setMessage("20文字を超えた場合は、あいまい検索しません。");
			}

		}

		// メッセージのセット
		result.setMessage(goPokedexList.isEmpty() ? MSG_NO_RESULTS : MessageFormat.format(MSG_RESULTS, goPokedexList.size()));

		// 1件のみヒットした場合
		if (goPokedexList.size() == 1) {
			result.setGoPokedex(goPokedexList.get(0));
			result.setUnique(true);
		}

		pokemonGoUtils.appendRemarks(goPokedexList);
		result.setGoPokedexList(goPokedexList);

		return result;
	}

	/**
	 * 独自のロジックで曖昧に検索します。
	 *
	 * @param transName
	 * @return
	 */
	private List<GoPokedex> searchFuzzy(String transName) {

		// 2文字単位で分割する。
		List<String> nameList = toFuzzyNameList(transName);
		// 検索
		return goPokedexRepository.findByNameIn(nameList);

	}


	/**
	 * 文字を2文字ずつに区切ったリストを返却する。
	 * あいうえお → [あい, いう, うえ, えお]
	 *
	 * @param name
	 * @return
	 */
	private List<String> toFuzzyNameList(String name) {

		char[] nameChars = name.toCharArray();

		List<String> list = new ArrayList<String>();
		for (int i = 0; i < nameChars.length; i++) {

			if (nameChars.length - i < 2) break;

			list.add(String.valueOf(nameChars[i]) + String.valueOf(nameChars[i + 1]));

		}

		return list;
	}
}
