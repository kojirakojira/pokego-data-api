package jp.brainjuice.pokego.business.service;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import com.atilika.kuromoji.TokenizerBase.Mode;
import com.atilika.kuromoji.ipadic.Token;
import com.atilika.kuromoji.ipadic.Tokenizer;
import com.atilika.kuromoji.ipadic.Tokenizer.Builder;
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
import jp.brainjuice.pokego.utils.BjUtils;
import jp.brainjuice.pokego.utils.exception.BadRequestException;
import jp.brainjuice.pokego.utils.exception.PokemonDataInitException;
import jp.brainjuice.pokego.web.form.req.research.ResearchRequest;
import jp.brainjuice.pokego.web.form.res.MsgLevelEnum;
import lombok.AllArgsConstructor;
import lombok.Data;

@Service
public class PokemonSearchService {

	private GoPokedexRepository goPokedexRepository;

	private PokedexFilterInfoRepository pokedexFilterInfoRepository;

	private PokemonGoUtils pokemonGoUtils;

	// Kuromoji形態素解析ライブラリのBuilderクラス
	private Builder tokenizerBuilder;

	// １つの単語がポケモンのグループを示す場合のポケモン名のリスト
	private Map<String, List<String>> groupFamiliarNameMap = new HashMap<>();

	// Kuromoji設定用のユーザ辞書
	private static final String DIC_FILE_NAME = "pokemon/pokemon-dictionary.csv";
	// １つの単語がポケモンのグループを示す場合の情報
	private static final String GROUP_FAMILIAR_NAME_FILE_NAME = "pokemon/pokemon-dictionary-group-familiar-name.yml";


	/** ひらカタ漢字は全角に、ＡＢＣ１２３は半角に */
	private Transliterator transAnyNFKC = Transliterator.getInstance("Any-NFKC");

	/** ひらがな→カタカナ */
	private Transliterator transHiraToKana = Transliterator.getInstance("Hiragana-Katakana");

	private static final String MSG_RESULTS = "{0}件のポケモンがヒットしました！";

	private static final String MSG_MAYBE = "なんだかよく分からなかったのでいい感じに検索しました！";

	private static final String MSG_NO_RESULTS = "該当するポケモンがいませんでした。";

	private static final String MSG_NO_ENTERED = "入力してください。";

	@SuppressWarnings("unchecked")
	@Autowired
	public PokemonSearchService(
			GoPokedexRepository goPokedexRepository,
			PokedexFilterInfoRepository pokedexFilterInfoRepository,
			PokemonGoUtils pokemonGoUtils) throws PokemonDataInitException {
		this.goPokedexRepository = goPokedexRepository;
		this.pokedexFilterInfoRepository = pokedexFilterInfoRepository;
		this.pokemonGoUtils = pokemonGoUtils;

		// Tokenizerを初期化
		try {
			Resource pokeDic = BjUtils.loadFile(DIC_FILE_NAME);

			tokenizerBuilder = new Tokenizer.Builder();
			tokenizerBuilder.mode(Mode.SEARCH);
			// ユーザ辞書に単語登録する。（カタカナをひらがなに変換した単語も登録する。
			tokenizerBuilder.userDictionary(getUserDictionary(pokeDic));
		} catch (IOException e) {
			throw new PokemonDataInitException("検索用の形態素解析モジュールの初期化に失敗しました。", e);
		}

		try {
			// pokemon-dictionry-group-familiar-name.ymlを読み込む
			groupFamiliarNameMap = BjUtils.loadYaml(GROUP_FAMILIAR_NAME_FILE_NAME, Map.class);

		} catch (IOException e) {
			throw new PokemonDataInitException(
					MessageFormat.format("{0}の読み込みに失敗しました。", GROUP_FAMILIAR_NAME_FILE_NAME),
					e);
		}

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

		// 形態素解析をして検索
		List<GoPokedex> goPokedexList = searchGeneral(words);
		result.setSearched(true);

		// 1件もヒットしなかった場合
		if (goPokedexList.isEmpty()) {

			if (words.length() <= 20) {
				// すごく曖昧に検索する。
				goPokedexList = searchFuzzy(words);
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
		TokenizeResult tokenizeResult = tokenize(words);
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

			List<GoPokedex> remarksList = goPokedexRepository.findByRemarksIn(otherTmpList);

			if (remarksList.size() <= 3) {
				goPokedexList = remarksList;
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

		return goPokedexList;
	}

	/**
	 * 引数に検索ワードを指定し、Kuromojiの形態素解析で分割する。
	 * 分割後、TokenizeResultを返却する。
	 *
	 * @param words
	 * @return
	 */
	private TokenizeResult tokenize(String words) {

		// Kuromoji Tokenizerの取得
		Tokenizer tokenizer = tokenizerBuilder.build();

		// 形態素解析をして分割する。
		List<Token> tokens = tokenizer.tokenize(words);

		// ポケモン名をリストで取得する。
		List<String> pokemonList = tokens.stream()
				.filter(t -> t.getPartOfSpeechLevel1().equals("pokemon"))
				.map(t -> t.getReading())
				.collect(Collectors.toList());

		// ポケモン名以外の名詞を取得する。
		List<String> otherList = tokens.stream()
				.filter(t -> t.getPartOfSpeechLevel1().equals("名詞"))
				.map(t -> t.getReading())
				.collect(Collectors.toList());

		// グループを示す単語（ブイズなど）からpokedexIdのリストを取得する。
		List<String> groupList = tokens.stream()
				.filter(t -> t.getPartOfSpeechLevel1().equals("group"))
				.flatMap(t -> groupFamiliarNameMap.get(t.getReading()).stream())
				.collect(Collectors.toList());

		// ポケモン名をリストで取得する。
		tokens.stream()
		.filter(t -> t.getPartOfSpeechLevel1().equals("複合名詞"))
		.forEach(t -> {
			// 複合名詞の場合は、再帰呼び出しして分解していく。（※無限ループしないように辞書の定義には気を付けること。）
			TokenizeResult compoundNounsResult = tokenize(t.getReading());
			pokemonList.addAll(compoundNounsResult.getPokemonList());
			otherList.addAll(compoundNounsResult.getOtherList());
			groupList.addAll(compoundNounsResult.getGroupList());
		});

		return new TokenizeResult(pokemonList, otherList, groupList);
	}

	/**
	 * Kuromojiの形態素解析アルゴリズムの結果を格納する。
	 *
	 * @author saibabanagchampa
	 *
	 */
	@Data
	@AllArgsConstructor
	private class TokenizeResult {
		// ユーザ辞書で品詞をpokemonにしたやつを入れる。
		private List<String> pokemonList;
		// ユーザ辞書で品詞を名詞にしたやつを入れる。
		private List<String> otherList;
		// ユーザ辞書で品詞をgroupにしたやつが入れる。
		private List<String> groupList;
	}

	/**
	 * 独自のロジックで曖昧に検索します。
	 *
	 * @param name
	 * @return
	 */
	private List<GoPokedex> searchFuzzy(String name) {

		// ひらがなをカタカナに置き換える。
		String transName;
		transName = transAnyNFKC.transliterate(name);
		transName = transHiraToKana.transliterate(transName);

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

	private InputStream getUserDictionary(Resource pokeDic) throws IOException {

		BufferedReader br = new BufferedReader(new InputStreamReader(pokeDic.getInputStream()));

		List<String> lineList = new ArrayList<>();
		{
			String line;
			while ((line = br.readLine()) != null) {
				lineList.add(line);
			}
			br.close();
		}

		// カタカナ→ひらがな
		Transliterator transKataToHira = Transliterator.getInstance("Katakana-Hiragana");

		// デフォルトの単語から、カタカナをひらがなに変換した単語を作成し、StringBuilderに変換する。
		StringBuilder sb = lineList.stream()
				.filter(line -> line.charAt(0) != '#') // コメント行を省く。
				.flatMap(line -> {
					int firstCommaIdx = line.indexOf(",");
					// 「単語」列のカタカナだけを、ひらがなに変換する。
					String firstElem = line.substring(0, firstCommaIdx);
					firstElem = transAnyNFKC.transliterate(firstElem);
					firstElem = transKataToHira.transliterate(firstElem);
					// 「単語」列に、無変換の別の列をくっつける
					String hiraganaLine = firstElem + line.substring(firstCommaIdx);
					// デフォルトの単語と、ひらがなに変換した単語の２つを単語登録する。
					Stream<String> stream = Stream.of(line);
					if (!line.equals(hiraganaLine)) {
						// カタ→ひら変換前と変換後が一致しない場合のみ２つの単語を登録する。
						stream = Stream.concat(stream, Stream.of(hiraganaLine));
					}
					return stream;
				})
				.map(line -> line + "\n") // 改行コードをいれて行扱いにする。
				.collect(StringBuilder::new, StringBuilder::append, StringBuilder::append);

		// StringBuilder→InputStreamに変換して返却。
		return new ByteArrayInputStream(sb.toString().getBytes("utf-8"));
	}
}
