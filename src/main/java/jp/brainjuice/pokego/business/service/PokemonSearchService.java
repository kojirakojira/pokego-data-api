package jp.brainjuice.pokego.business.service;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ibm.icu.text.Transliterator;

import jp.brainjuice.pokego.business.dao.GoPokedexRepository;
import jp.brainjuice.pokego.business.dao.PokedexFilterInfoRepository;
import jp.brainjuice.pokego.business.dao.PokedexFilterInfoRepository.FilterEnum;
import jp.brainjuice.pokego.business.dao.dto.FilterParam;
import jp.brainjuice.pokego.business.dao.entity.GoPokedex;
import jp.brainjuice.pokego.business.service.utils.PokemonFilterValueUtils;
import jp.brainjuice.pokego.business.service.utils.PokemonGoUtils;
import jp.brainjuice.pokego.business.service.utils.dto.GoPokedexAndCp;
import jp.brainjuice.pokego.business.service.utils.dto.MultiSearchResult;
import jp.brainjuice.pokego.business.service.utils.dto.PokemonFilterResult;
import jp.brainjuice.pokego.business.service.utils.dto.PokemonSearchResult;
import jp.brainjuice.pokego.utils.exception.BadRequestException;
import jp.brainjuice.pokego.web.form.req.research.ResearchRequest;
import jp.brainjuice.pokego.web.form.res.MsgLevelEnum;

@Service
public class PokemonSearchService {

	private GoPokedexRepository goPokedexRepository;

	private PokedexFilterInfoRepository pokedexFilterInfoRepository;

	private PokemonGoUtils pokemonGoUtils;

	/** ひらカタ漢字は全角に、ＡＢＣ１２３は半角に */
	private Transliterator transAnyNFKC = Transliterator.getInstance("Any-NFKC");

	/** ひらがな→カタカナ */
	private Transliterator transHiraToKana = Transliterator.getInstance("Hiragana-Katakana");

	private static final String MSG_RESULTS = "{0}件のポケモンがヒットしました！";

	private static final String MSG_NO_RESULTS = "該当するポケモンがいませんでした。";

	private static final String MSG_NO_ENTERED = "入力してください。";

	@Autowired
	public PokemonSearchService(
			GoPokedexRepository goPokedexRepository,
			PokedexFilterInfoRepository pokedexFilterInfoRepository,
			PokemonGoUtils pokemonGoUtils) {
		this.goPokedexRepository = goPokedexRepository;
		this.pokedexFilterInfoRepository = pokedexFilterInfoRepository;
		this.pokemonGoUtils = pokemonGoUtils;

	}

	/**
	 * ポケモンを絞り込みます。
	 *
	 * @param req
	 * @return
	 */
	public PokemonFilterResult filter(ResearchRequest req) {

		PokemonFilterResult result = new PokemonFilterResult();

		// 絞り込み検索値の取得
		Map<FilterEnum, FilterParam> filterMap = PokemonFilterValueUtils.mapping(PokemonFilterValueUtils.createPokemonFilterValue(req));

		// 画面表示用の絞り込み検索値のセット
		result.setFilteredItems(PokemonFilterValueUtils.convDisp(filterMap));

		// 絞り込み
		List<String> pokedexIdList = pokedexFilterInfoRepository.findByAny(filterMap);
		// GoPokedexの取得
		List<GoPokedex> goPokedexList = goPokedexRepository.findAllById(pokedexIdList);


		if (goPokedexList.isEmpty()) {
			// 検索結果なしだった場合
			result.setMessage(MSG_NO_RESULTS);

		} else {
			// 空でない場合
			result.setHit(true);
			result.setMessage(MessageFormat.format(MSG_RESULTS, goPokedexList.size()));

			if (goPokedexList.size() == 1) {
				// 1件のみヒットした場合
				result.setGoPokedex(getGoPokedexAndCp(goPokedexList.get(0)));
				result.setUnique(true);
			}
		}

		List<GoPokedexAndCp> gpAndCpList = goPokedexList.stream()
				.map(this::getGoPokedexAndCp)
				.collect(Collectors.toList());
		result.setGpAndCpList(gpAndCpList);
		return result;
	}

	private GoPokedexAndCp getGoPokedexAndCp(GoPokedex goPokedex) {
		int cp = pokemonGoUtils.calcBaseCp(goPokedex.getAttack(), goPokedex.getDefense(), goPokedex.getHp());
		GoPokedexAndCp gpAndCp = new GoPokedexAndCp(goPokedex, cp);
		return gpAndCp;
	}

	/**
	 * GoPokedexに該当するポケモンが存在するか検索します。
	 *
	 * @param nameList
	 * @return
	 */
	public MultiSearchResult multiSearch(List<String> nameList) throws BadRequestException {

		MultiSearchResult res = new MultiSearchResult();
		res.setMessage("");

		// 1件ずつ検索していく。
		List<PokemonSearchResult> psrList = new ArrayList<>();
		nameList.forEach(n -> {
			PokemonSearchResult psr = search(n);
			psrList.add(psr);
		});
		res.setPsrArr(psrList);

		// 検索結果に応じた処理
		if (psrList.stream().filter(psr -> { return !psr.isSearched(); }).anyMatch(e -> true)) {
			// 未入力のnameが存在する場合。(Stream#anyMatch(e -> true)でStream版のisEmpty()になる。）
			res.setMessage(MSG_NO_ENTERED);
			res.setMsgLevel(MsgLevelEnum.error);

		} else if (psrList.stream().filter(psr -> { return !psr.isHit(); }).anyMatch(e -> true)) {
			// 検索結果なしのnameが存在する場合。
			res.setMessage(MSG_NO_RESULTS);
			res.setMsgLevel(MsgLevelEnum.error);

		} else if (!psrList.stream().filter(psr -> { return !psr.isUnique(); }).anyMatch(e -> true)) {
			// psrListが全て一意になる場合。（ユニークじゃないやつがない存在しない場合。）
			res.setAllUnique(true);

		}

		return res;
	}

	/**
	 * GoPokedexに該当するポケモンが存在するか検索します。
	 *
	 * @param name
	 * @return
	 */
	public PokemonSearchResult search(String name) {

		PokemonSearchResult result = new PokemonSearchResult();

		if (StringUtils.isEmpty(name)) {
			result.setMessage(MSG_NO_ENTERED);
			return result;
		}

		String transName;
		transName = transAnyNFKC.transliterate(name);
		transName = transHiraToKana.transliterate(transName);

		// 検索
		List<GoPokedex> goPokedexList = goPokedexRepository.findByNameIn(Arrays.asList(transName, "メガ" + transName));
		result.setSearched(true);

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

		if (goPokedexList.isEmpty()) {
			// 最終的に検索結果なしだった場合
			result.setMessage(MSG_NO_RESULTS);

		} else {
			// 空でない場合
			result.setHit(true);
			result.setMessage(MessageFormat.format(MSG_RESULTS, goPokedexList.size()));

			if (goPokedexList.size() == 1) {
				// 1件のみヒットした場合
				result.setGoPokedex(goPokedexList.get(0));
				result.setUnique(true);
			}
		}

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
