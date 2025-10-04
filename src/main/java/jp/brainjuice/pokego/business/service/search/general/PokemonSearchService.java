package jp.brainjuice.pokego.business.service.search.general;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jp.brainjuice.pokego.business.service.search.pokeFilter.FilterEnum;
import jp.brainjuice.pokego.business.service.search.pokeFilter.GoPokedexFilterService;
import jp.brainjuice.pokego.business.service.search.pokeFilter.PokemonFilterResult;
import jp.brainjuice.pokego.business.service.search.pokeFilter.PokemonFilterValue;
import jp.brainjuice.pokego.business.service.search.pokeFilter.PokemonFilterValueUtils;
import jp.brainjuice.pokego.business.service.search.utils.PokemonEditUtils;
import jp.brainjuice.pokego.business.service.search.utils.PokemonGoUtils;
import jp.brainjuice.pokego.business.service.search.utils.dto.GoPokedexAndCp;
import jp.brainjuice.pokego.business.service.search.utils.dto.MultiSearchResult;
import jp.brainjuice.pokego.business.service.search.utils.dto.PokemonSearchResult;
import jp.brainjuice.pokego.business.service.search.utils.dto.TokenizeResult;
import jp.brainjuice.pokego.cache.inmemory.PokemonDictionaryInfo;
import jp.brainjuice.pokego.dao.jpa.GoPokedexRepository;
import jp.brainjuice.pokego.dao.jpa.dto.FilterParam;
import jp.brainjuice.pokego.dao.jpa.entity.GoPokedex;
import jp.brainjuice.pokego.utils.BjUtils;
import jp.brainjuice.pokego.utils.exception.PokemonDataInitException;
import jp.brainjuice.pokego.web.search.form.req.ResearchRequest;
import jp.brainjuice.pokego.web.search.form.res.MsgLevelEnum;
import jp.brainjuice.pokego.web.search.form.res.elem.PidAndName;
import lombok.AllArgsConstructor;
import lombok.Data;

@Service
public class PokemonSearchService {

	private GoPokedexRepository goPokedexRepository;

	private PokemonGoUtils pokemonGoUtils;

	private PokemonDictionaryInfo pokemonDictionaryInfo;

	private GoPokedexFilterService goPokedexFilterService;

	private static final String MSG_RESULTS = "{0}件のポケモンがヒットしました！";

	private static final String MSG_MAYBE = "なんだかよく分からなかったのでいい感じに検索しました！";

	private static final String MSG_NO_RESULTS = "該当するポケモンがいませんでした。";

	private static final String MSG_NO_ENTERED = "入力してください。";

	public PokemonSearchService(
			GoPokedexRepository goPokedexRepository,
			PokemonGoUtils pokemonGoUtils,
			PokemonDictionaryInfo pokemonDictionaryInfo,
			GoPokedexFilterService goPokedexFilterService) throws PokemonDataInitException {
		this.goPokedexRepository = goPokedexRepository;
		this.pokemonGoUtils = pokemonGoUtils;
		this.pokemonDictionaryInfo = pokemonDictionaryInfo;
		this.goPokedexFilterService = goPokedexFilterService;

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
		Map<FilterEnum, FilterParam> filterMap = PokemonFilterValueUtils.mapping(new PokemonFilterValue(req));

		// 画面表示用の絞り込み検索値のセット
		result.setFilteredItems(PokemonFilterValueUtils.convDisp(filterMap));

		// GoPokedexの取得
		List<GoPokedex> goPokedexList = goPokedexFilterService.findByAny(filterMap);


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
				result.setGoPokedex(getGoPokedexAndCp(goPokedexList.get(0), 0));
				result.setUnique(true);
			}
		}

		AtomicInteger counter = new AtomicInteger();
		List<GoPokedexAndCp> gpAndCpList = goPokedexList.stream()
				.map(gp -> getGoPokedexAndCp(gp, counter.incrementAndGet()))
				.collect(Collectors.toList());
		result.setGpAndCpList(gpAndCpList);
		return result;
	}

	private GoPokedexAndCp getGoPokedexAndCp(GoPokedex goPokedex, int no) {
		int cp = pokemonGoUtils.calcBaseCp(goPokedex.getAttack(), goPokedex.getDefense(), goPokedex.getHp());
		GoPokedexAndCp gpAndCp = new GoPokedexAndCp(no, goPokedex, cp);
		return gpAndCp;
	}

	@Data
	@AllArgsConstructor
	private class MultiSearchDto {
		private String pid;
		private PokemonSearchResult psr;
	}

	/**
	 * GoPokedexに該当するポケモンが存在するか検索します。
	 *
	 * @param nameList
	 * @return
	 */
	@Transactional(readOnly = true)
	public MultiSearchResult multiSearch(List<PidAndName> pidAndNameList) {

		MultiSearchResult res = new MultiSearchResult();
		res.setMessage("");

		List<MultiSearchDto> msDtoList = pidAndNameList.stream()
				.map(pan -> {
					PokemonSearchResult psr = null;
					if (StringUtils.isEmpty(pan.getPid())) {
						psr = search(pan.getName());
					}
					return new MultiSearchDto(pan.getPid(), psr);
				})
				.toList();

		// 既にpidが確定しているもの
		List<String> pidList = msDtoList.stream()
				.map(MultiSearchDto::getPid)
				.filter(StringUtils::isNotEmpty)
				.toList();
		final List<GoPokedex> gpList = goPokedexRepository.findAllById(pidList);

		List<PokemonSearchResult> psrList = msDtoList.stream()
				.map(msDto -> {
					PokemonSearchResult psr = msDto.getPsr();
					if (!StringUtils.isEmpty(msDto.getPid())) {
						// pidがある場合は、psrが存在しない。
						GoPokedex goPokedex = gpList.stream()
								.filter(gp -> gp.getPokedexId().equals(msDto.getPid()))
								.findAny().orElseThrow();
						psr = createUniquePsr(goPokedex);
					}
					return psr;
				})
				.toList();
		res.setPsrArr(psrList);

		// 検索結果に応じた処理
		if (psrList.stream()
				.filter(psr -> !psr.isSearched())
				.anyMatch(e -> true)) {

			// 未入力のnameが存在する場合
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

			// psrListが全て一意になる場合。（ユニークじゃないやつが存在しない場合。）
			res.setAllUnique(true);

		}

		return res;
	}

	/**
	 * ユニークなpsrを生成する
	 *
	 * @param goPokedex
	 * @return
	 */
	public PokemonSearchResult createUniquePsr(GoPokedex goPokedex) {

		PokemonSearchResult psr = new PokemonSearchResult();
		psr.setUnique(true);
		psr.setGoPokedex(goPokedex);
		psr.setMaybe(false);
		psr.setHit(true); // ヒットしたものとする
		psr.setSearched(true); // 検索したものとする

		return psr;
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

		// 形態素解析をして検索
		List<GoPokedex> goPokedexList = searchGeneral(words);
		result.setSearched(true);

		// 1件もヒットしなかった場合
		if (goPokedexList.isEmpty()) {

			if (words.length() <= 20) {
				// ひらがなをカタカナに置き換える。
				// 例「あア亜１ｱ1」→「アア亜1ア1」
				String transWords = BjUtils.transAnyNFKC(words);
				transWords = BjUtils.transHiraToKana(transWords);
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
	 * @param transWords
	 * @return
	 */
	private List<GoPokedex> searchGeneral(String words) {

		TokenizeResult tokenizeResult = pokemonDictionaryInfo.search(words);
		// 形態素解析で分解
		final List<String> pokemonList = tokenizeResult.getPokemonList();
		final List<String> otherList = tokenizeResult.getOtherList();
		final List<String> groupList = tokenizeResult.getGroupList();

		/* 以下、GoPokedexの検索アルゴリズム */
		// ポケモン名からGoPokedexリストを取得
		List<GoPokedex> goPokedexList = goPokedexRepository.findByNameLikeIn(
				pokemonList.stream().map(BjUtils::wrapWithPercent).toArray(String[]::new));

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

			// まず、入力された文字列から、そのまま備考を検索する。
			goPokedexList = searchRemarks(List.of(words));

			if (goPokedexList.isEmpty()) {
				// ない場合は、形態素解析して、名詞判定された値から備考を検索する。
				goPokedexList = searchRemarks(otherList);
			}

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

		// 昇順に並び替えて返却
		return goPokedexList.stream().sorted(PokemonEditUtils.getPokedexComparator(1)).toList();
	}

	/**
	 * 独自のロジックで曖昧に検索します。
	 *
	 * @param name
	 * @return
	 */
	private List<GoPokedex> searchFuzzy(String name) {

		// 2文字単位で分割する。
		String[] nameArr = toFuzzyNameList(name).stream()
				.map(BjUtils::wrapWithPercent)
				.toArray(String[]::new);
		// 検索
		return goPokedexRepository.findByNameLikeIn(nameArr);

	}

	/**
	 * 文字を2文字ずつに区切ったリストを返却する。
	 * あいうえお → [あい, いう, うえ, えお]
	 *
	 * @param name
	 * @return
	 */
	private List<String> toFuzzyNameList(String name) {

		if (name.length() < 2) {
			return List.of(name);
		}

		char[] nameChars = name.toCharArray();

		List<String> list = new ArrayList<String>();
		for (int i = 0; i < nameChars.length; i++) {

			if (nameChars.length - i < 2) break;

			list.add(String.valueOf(nameChars[i]) + String.valueOf(nameChars[i + 1]));

		}

		return list;
	}

	/**
	 * 備考から部分一致検索します。
	 *
	 * @param wordList
	 * @return
	 */
	private List<GoPokedex> searchRemarks(List<String> wordList) {

		String[] otherTmpArr = wordList.stream()
				.filter(o -> 2 < o.length())
				.map(BjUtils::wrapWithPercent)
				.toArray(String[]::new);

		return goPokedexRepository.findByRemarksContaining(otherTmpArr);
	}
}
