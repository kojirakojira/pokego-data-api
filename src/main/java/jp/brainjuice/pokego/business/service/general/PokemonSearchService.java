package jp.brainjuice.pokego.business.service.general;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jp.brainjuice.pokego.business.dao.GoPokedexRepository;
import jp.brainjuice.pokego.business.dao.PokedexSpecifications.FilterEnum;
import jp.brainjuice.pokego.business.dao.dto.FilterParam;
import jp.brainjuice.pokego.business.dao.entity.GoPokedex;
import jp.brainjuice.pokego.business.service.utils.PokemonFilterValueUtils;
import jp.brainjuice.pokego.business.service.utils.PokemonGoUtils;
import jp.brainjuice.pokego.business.service.utils.dto.GoPokedexAndCp;
import jp.brainjuice.pokego.business.service.utils.dto.MultiSearchResult;
import jp.brainjuice.pokego.business.service.utils.dto.PokemonFilterResult;
import jp.brainjuice.pokego.business.service.utils.dto.PokemonSearchResult;
import jp.brainjuice.pokego.business.service.utils.dto.TokenizeResult;
import jp.brainjuice.pokego.business.service.utils.memory.PokemonDictionaryInfo;
import jp.brainjuice.pokego.utils.BjUtils;
import jp.brainjuice.pokego.utils.exception.BadRequestException;
import jp.brainjuice.pokego.utils.exception.PokemonDataInitException;
import jp.brainjuice.pokego.web.form.req.ResearchRequest;
import jp.brainjuice.pokego.web.form.res.MsgLevelEnum;

@Service
public class PokemonSearchService {

	private GoPokedexRepository goPokedexRepository;

	private PokemonGoUtils pokemonGoUtils;

	private PokemonDictionaryInfo pokemonDictionaryInfo;

	private static final String MSG_RESULTS = "{0}件のポケモンがヒットしました！";

	private static final String MSG_MAYBE = "なんだかよく分からなかったのでいい感じに検索しました！";

	private static final String MSG_NO_RESULTS = "該当するポケモンがいませんでした。";

	private static final String MSG_NO_ENTERED = "入力してください。";

	@Autowired
	public PokemonSearchService(
			GoPokedexRepository goPokedexRepository,
			PokemonGoUtils pokemonGoUtils,
			PokemonDictionaryInfo pokemonDictionaryInfo) throws PokemonDataInitException {
		this.goPokedexRepository = goPokedexRepository;
		this.pokemonGoUtils = pokemonGoUtils;
		this.pokemonDictionaryInfo = pokemonDictionaryInfo;

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

		// GoPokedexの取得
		List<GoPokedex> goPokedexList = goPokedexRepository.findByAny(filterMap);


		if (goPokedexList.isEmpty()) {
			// 検索結果なしだった場合
			result.setMsgLevel(MsgLevelEnum.error);
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
		List<PokemonSearchResult> psrList = nameList.stream()
				.map(this::search)
				.collect(Collectors.toList());

		res.setPsrArr(psrList);

		// 検索結果に応じた処理
		if (psrList.stream()
				.filter(psr -> !psr.isSearched())
				.anyMatch(e -> true)) {

			// 未入力のnameが存在する場合。(Stream#anyMatch(e -> true)でStream版のisEmpty()になる。）
			res.setMessage(MSG_NO_ENTERED);
			res.setMsgLevel(MsgLevelEnum.error);

		} else if (psrList.stream()
				.filter(psr -> !psr.isHit())
				.anyMatch(e -> true)) {

			// 検索結果なしのnameが存在する場合。
			res.setMessage(MSG_NO_RESULTS);
			res.setMsgLevel(MsgLevelEnum.error);

		} else if (!psrList.stream()
				.filter(psr -> !psr.isUnique())
				.anyMatch(e -> true)) {

			// psrListが全て一意になる場合。（ユニークじゃないやつがない存在しない場合。）
			res.setAllUnique(true);

		}

		return res;
	}

	/**
	 * GoPokedexに該当するポケモンが存在するか検索します。
	 *
	 * @param words
	 * @return
	 */
	public PokemonSearchResult search(String words) {

		PokemonSearchResult result = new PokemonSearchResult();

		if (StringUtils.isEmpty(words)) {
			result.setMessage(MSG_NO_ENTERED);
			result.setMsgLevel(MsgLevelEnum.error);
			return result;
		}

		// ひらがなをカタカナに置き換える。
		// 例「あア亜１ｱ1」→「アア亜1ア1」
		String transWords = BjUtils.transAnyNFKC(words);
		transWords = BjUtils.transHiraToKana(transWords);

		// 形態素解析をして検索
		List<GoPokedex> goPokedexList = searchGeneral(transWords);
		result.setSearched(true);

		// 1件もヒットしなかった場合
		if (goPokedexList.isEmpty()) {

			if (words.length() <= 20) {
				// すごく曖昧に検索する。
				goPokedexList = searchFuzzy(transWords);
				result.setMaybe(true);
			} else {
				// なんか負荷がかかりそうだから文字数が多いときは検索させない。
				result.setMessage("20文字を超えた場合は、あいまい検索しません。");
				result.setMsgLevel(MsgLevelEnum.error);
			}

		}

		if (goPokedexList.isEmpty()) {
			// 最終的に検索結果なしだった場合
			result.setMessage(MSG_NO_RESULTS);
			result.setMsgLevel(MsgLevelEnum.error);

		} else {
			// 空でない場合
			result.setHit(true);
			// メッセージの作成
			String msg = MessageFormat.format(MSG_RESULTS, goPokedexList.size());
			msg = result.isMaybe() ? msg + MSG_MAYBE : msg;
			result.setMessage(msg);

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
	 * 入力された文字列を形態素解析で分解し、名詞（ポケモン名、それ以外）から検索をおこなう。
	 *
	 * @param words
	 * @return
	 */
	private List<GoPokedex> searchGeneral(String words) {

		// 形態素解析で分解
		TokenizeResult tokenizeResult = pokemonDictionaryInfo.search(words);
		List<String> pokemonList = tokenizeResult.getPokemonList();
		List<String> otherList = tokenizeResult.getOtherList();
		List<String> groupList = tokenizeResult.getGroupList();

		/* 以下、GoPokedexの検索アルゴリズム */

		// ポケモン名からGoPokedexリストを取得
		List<GoPokedex> goPokedexList = goPokedexRepository.findByNameIn(pokemonList);

		// groupListが空でない場合、goPokedexListにがっちゃんこする。
		if (!groupList.isEmpty()) {
			List<GoPokedex> groupGoPdList = goPokedexRepository.findAllById(groupList);

			goPokedexList = Stream.concat(
					goPokedexList.stream(),
					groupGoPdList.stream())
					.distinct()
					.collect(Collectors.toList());
		}

		if (goPokedexList.isEmpty()) {
			// ポケモン名がヒットしなかった場合

			List<String> otherTmpList = otherList.stream()
					.filter(o -> 2 < o.length())
					.collect(Collectors.toList());

			goPokedexList = goPokedexRepository.findByRemarksIn(otherTmpList);

		} else if (!otherList.isEmpty()) {
			// ポケモン名以外の名詞が存在する場合

			// 備考で絞り込む
			List<GoPokedex> remarksResultList = goPokedexList.stream()
					.filter(gp -> otherList.stream()
							.filter(other ->  gp.getRemarks().contains(other))
							.anyMatch(e -> true))
					.collect(Collectors.toList());

			if (!remarksResultList.isEmpty()) {
				// 備考で絞り込んだ結果が0件にならなければ、絞り込んだ結果を検索結果とする。
				goPokedexList = remarksResultList;
			}
		}

		return goPokedexList;
	}

	/**
	 * 独自のロジックで曖昧に検索します。
	 *
	 * @param name
	 * @return
	 */
	private List<GoPokedex> searchFuzzy(String name) {

		// 2文字単位で分割する。
		List<String> nameList = toFuzzyNameList(name);
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
