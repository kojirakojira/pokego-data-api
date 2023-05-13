package jp.brainjuice.pokego;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;

import com.atilika.kuromoji.TokenizerBase.Mode;
import com.atilika.kuromoji.ipadic.Token;
import com.atilika.kuromoji.ipadic.Tokenizer;
import com.atilika.kuromoji.ipadic.Tokenizer.Builder;
import com.ibm.icu.text.Transliterator;

public class Tests {

	@Test
	public void testCase001() throws Exception {

		DefaultResourceLoader resourceLoader = new DefaultResourceLoader();
		Resource pokeDic = resourceLoader.getResource("classpath:" + "pokemon/pokemon-dictionary.csv");

		Builder tokenizerBuilder = null;
		try {
			tokenizerBuilder = new Tokenizer.Builder();
			tokenizerBuilder.mode(Mode.SEARCH);
			// ユーザ辞書に単語登録する。（カタカナをひらがなに変換した単語も登録する。
			tokenizerBuilder.userDictionary(getUserDictionary(pokeDic));
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}

        Tokenizer tokenizer = tokenizerBuilder.build();

		String input = "ぴんくでぶのピンクデブは青。";
        List<Token> tokens = tokenizer.tokenize(input);
        List<String> wordList = tokens.stream()
        		.map(t -> t.getSurface() + " " + t.getAllFeatures())
        		.collect(Collectors.toList());

        wordList.forEach(System.out::println);


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

		Transliterator transAnyNFKC = Transliterator.getInstance("Any-NFKC");
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
					return Stream.of(line, hiraganaLine);
				})
				.map(line -> line + "\n")
				.collect(StringBuilder::new, StringBuilder::append, StringBuilder::append);

		// StringBuilder→InputStreamに変換して返却。
		return new ByteArrayInputStream(sb.toString().getBytes("utf-8"));
	}
}
