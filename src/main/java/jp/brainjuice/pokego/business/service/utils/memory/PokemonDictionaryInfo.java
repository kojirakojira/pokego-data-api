package jp.brainjuice.pokego.business.service.utils.memory;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import com.atilika.kuromoji.TokenizerBase.Mode;
import com.atilika.kuromoji.ipadic.Token;
import com.atilika.kuromoji.ipadic.Tokenizer;
import com.atilika.kuromoji.ipadic.Tokenizer.Builder;

import jp.brainjuice.pokego.business.service.utils.dto.TokenizeResult;
import jp.brainjuice.pokego.utils.BjUtils;
import jp.brainjuice.pokego.utils.exception.PokemonDataInitException;
import lombok.extern.slf4j.Slf4j;

/**
 * ポケモンの別名・俗称を保持する。
 *
 * @author saibabanagchampa
 *
 */
@Component
@Slf4j
public class PokemonDictionaryInfo {

	/** Kuromoji形態素解析ライブラリのBuilderクラス */
	private Builder tokenizerBuilder;

	/** １つの単語がポケモンのグループを示す場合のポケモン名のリスト */
	private Map<String, List<String>> groupFamiliarNameMap;

	// Kuromoji設定用のユーザ辞書
	private static final String DIC_FILE_NAME = "pokemon/pokemon-dictionary.csv";
	// １つの単語がポケモンのグループを示す場合の情報
	private static final String GROUP_FAMILIAR_NAME_FILE_NAME = "pokemon/pokemon-dictionary-group-familiar-name.yml";

	public PokemonDictionaryInfo() throws PokemonDataInitException {
		init();
	}

	public List<Token> getTokens(String words) {

		// Kuromoji Tokenizerの取得
		Tokenizer tokenizer = tokenizerBuilder.build();

		// 形態素解析をして分割する。
		return tokenizer.tokenize(words);
	}

	/**
	 * 引数に検索ワードを指定し、Kuromojiの形態素解析で分割後、合致する条件を取得する。
	 * 分割後、TokenizeResultを返却する。
	 *
	 * @param words
	 * @return
	 */
	public TokenizeResult search(String words) {

		log.debug("words: " + words);

		List<Token> tokens = getTokens(words);

		return search(tokens);
	}

	/**
	 * 引数にTokenのリストを指定し、Kuromojiの形態素解析で分割後、合致する条件を取得する。
	 * 分割後、TokenizeResultを返却する。
	 *
	 * @param tokens
	 * @return
	 */
	public TokenizeResult search(List<Token> tokens) {

		log.debug("tokens: " + tokens);

		// ポケモン名をリストで取得する。
		List<String> pokemonList = tokens.stream()
				.filter(t -> t.getPartOfSpeechLevel1().equals("pokemon"))
				.map(t -> t.getReading())
				.collect(Collectors.toList());

		// ポケモン名以外の名詞を取得する。
		List<String> otherList = tokens.stream()
				.filter(t -> t.getPartOfSpeechLevel1().equals("名詞"))
				.filter(t -> !t.getReading().equals("*"))
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
			TokenizeResult compoundNounsResult = search(t.getReading());
			pokemonList.addAll(compoundNounsResult.getPokemonList());
			otherList.addAll(compoundNounsResult.getOtherList());
			groupList.addAll(compoundNounsResult.getGroupList());
		});

		return new TokenizeResult(pokemonList, otherList, groupList);
	}

	@SuppressWarnings("unchecked")
	public void init() throws PokemonDataInitException {

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
		log.info(MessageFormat.format(
				"PokemonDictionaryInfo generated!! (Referenced file: resources/{0}, resources/{1})",
				DIC_FILE_NAME,
				GROUP_FAMILIAR_NAME_FILE_NAME));
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

		// デフォルトの単語から、ひらがなをカタカナに変換した単語を作成し、StringBuilderに変換する。
		StringBuilder sb = lineList.stream()
				.filter(line -> line.charAt(0) != '#') // コメント行を省く。
				.map(line -> {
					int firstCommaIdx = line.indexOf(",");
					// 「単語」列のカタカナだけを、ひらがなに変換する。
					String firstElem = line.substring(0, firstCommaIdx);
					firstElem = BjUtils.transAnyNFKC(firstElem);
					firstElem = BjUtils.transHiraToKana(firstElem);
					// 「単語」列に、無変換の別の列をくっつける
					String hiraganaLine = firstElem + line.substring(firstCommaIdx);
					return hiraganaLine;
				})
				.map(line -> line + "\n") // 改行コードをいれて行扱いにする。
				.distinct() // 重複削除
				.collect(StringBuilder::new, StringBuilder::append, StringBuilder::append);

		// StringBuilder→InputStreamに変換して返却。
		log.debug("UserDictionary: " + sb.toString());
		return new ByteArrayInputStream(sb.toString().getBytes("utf-8"));
	}
}
