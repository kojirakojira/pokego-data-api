package jp.brainjuice.pokego.business.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.ibm.icu.text.Transliterator;

import jp.brainjuice.pokego.business.dao.GoPokedexRepository;
import jp.brainjuice.pokego.business.dao.GoPokedexSpecification;
import jp.brainjuice.pokego.business.dao.entity.GoPokedex;
import jp.brainjuice.pokego.business.service.utils.dto.PokemonSearchResult;

@Service
public class PokemonSearchService {

	private GoPokedexRepository goPokedexRepository;

	/** ひらカタ漢字は全角に、ＡＢＣ１２３は半角に */
	private Transliterator transAnyNFKC = Transliterator.getInstance("Any-NFKC");

	/** ひらがな→カタカナ */
	private Transliterator transHiraToKana = Transliterator.getInstance("Hiragana-Katakana");

	@Autowired
	public PokemonSearchService(GoPokedexRepository goPokedexRepository) {
		this.goPokedexRepository = goPokedexRepository;
	}

	@Transactional(readOnly = true, isolation=Isolation.READ_UNCOMMITTED)
	public PokemonSearchResult search(String name) {

		PokemonSearchResult result = new PokemonSearchResult();

		String transName;
		transName = transAnyNFKC.transliterate(name);
		transName = transHiraToKana.transliterate(transName);

		List<GoPokedex> goPokedexList = goPokedexRepository.findByNameIn(Arrays.asList(transName, "メガ" + transName));

		if (goPokedexList.size() == 1) {
			// 1件のみヒットした場合
			result.setUnique(true);
			result.setGoPokedex(goPokedexList.get(0));
			return result;
		} else if (goPokedexList.size() == 0) {

			// 1件もヒットしなかった場合、すごく曖昧に検索する。
			if (transName.length() <= 10) {
				goPokedexList = searchFuzzy(transName);
				result.setMaybe(true);
			} else {
				// なんかDBに負荷がかかりそうだから文字数が多いときは検索させない。
				result.setMessage("10文字を超えた場合は、あいまい検索しません。");
			}

		}

		result.setUnique(false);
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

		GoPokedexSpecification<GoPokedex> goPokeSpec = new GoPokedexSpecification<GoPokedex>();

		// name like '%あい%' or name like '%いう%' or ...と連結させていく。
		Specification<GoPokedex> spec = null;
		for (int i = 0; i < nameList.size(); i++) {
			spec = goPokeSpec.nameContains(nameList.get(i));
			spec = spec.or(spec);
		}

		// 検索
		return goPokedexRepository.findAll(Specification.where(spec));

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
