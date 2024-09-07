package jp.brainjuice.pokego.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.yaml.snakeyaml.Yaml;

import com.ibm.icu.text.Transliterator;

import jp.brainjuice.pokego.filter.log.LogUtils;

public final class BjUtils {

	public static final Locale locale = Locale.JAPAN;

	public static final String sdfYmdhme = "yyyy/MM/dd(E) HH:mm";
	public static final String sdfYmdhms = "yyyy-MM-dd HH:mm:ss";
	public static final String sdfYmdhm = "yyyy/MM/dd HH:mm";
	public static final String sdfYmd = "yyyy/MM/dd";
	public static final String sdfMde = "MM/dd(E)";
	public static final String sdfHm = "HH:mm";
	public static final String dirFormat = "yyyyMMddHHmmss";

	/** ひらカタ漢字は全角に、ＡＢＣ１２３は半角に */
	private static Transliterator transAnyNFKC = Transliterator.getInstance("Any-NFKC");

	/** ひらがな→カタカナ */
	private static Transliterator transHiraToKana = Transliterator.getInstance("Hiragana-Katakana");

	/** カタカナ→ひらがな */
	private static Transliterator transKanaToHira = Transliterator.getInstance("Katakana-Hiragana");

	/**
	 * 引数に指定された文字が、空文字またはnullでない場合は数値に変換し返却する。
	 *
	 * @param str
	 * @return
	 */
	public static Integer parseInt(String str) {

		Integer value = null;
		if (!StringUtils.isEmpty(str)) {
			value = Integer.valueOf(str);
		}
		return value;
	}

	/**
	 * String型→Date型変換
	 *
	 * @param str
	 * @param format
	 * @return
	 * @throws Exception
	 */
	public static Date parseDate(String str, String format) {

		return parseDate(str, new SimpleDateFormat(format, locale));
	}

	/**
	 * String型→Date型変換
	 *
	 * @param str
	 * @param format
	 * @return
	 * @throws Exception
	 */
	public static Date parseDate(String str, SimpleDateFormat sdf) {

		if (StringUtils.isEmpty(str)) {
			return null;
		}

		Date date;
		try {
			date = sdf.parse(str);
		} catch (ParseException pe) {
			date = null;
			LogUtils.getLog(BjUtils.class).debug("Conversion failed. Text: " + str, pe);
		}
		return date;
	}

	/**
	 * Date→String変換
	 *
	 * @param date
	 * @param format
	 * @return
	 */
	public static String formatDate(Date date, SimpleDateFormat sdf) {

		if (date == null) {
			return "";
		}

		return sdf.format(date);

	}

	/**
	 * Date→String変換
	 *
	 * @param date
	 * @param format
	 * @return
	 */
	public static String formatDate(Date date, String format) {

		return formatDate(date, new SimpleDateFormat(format, locale));
	}

	/**
	 * 環境変数にNOW_DATE="yyyy-MM-dd HH:mm:ss"を設定した場合、現在日付を変更できます。
	 *
	 * @return
	 */
	public static Date now() {

		Date now = new Date();
		String env = System.getenv("BRAINJUICE_NOW_DATE");
		if (!StringUtils.isEmpty(env)) {
			now = parseDate(env, sdfYmdhms);
		}
		return now;
	}

	/**
	 * Date→LocalDateTime変換
	 * （Date→Instant→LocalDateTime）
	 *
	 * @param date
	 * @return
	 */
	public static LocalDateTime toLocalDateTime(Date date) {

		if (date == null) {
			return null;
		}

		return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
	}

	/**
	 * LocalDateTime→Date変換<br>
	 * （LocalDateTime→ZonedDateTime→Instant→Date）
	 *
	 * @param localDateTime
	 * @return
	 */
	public static Date toDate(LocalDateTime localDateTime) {

		if (localDateTime == null) {
			return null;
		}

		ZonedDateTime zonedDateTime = ZonedDateTime.of(localDateTime, ZoneId.systemDefault());
		return Date.from(zonedDateTime.toInstant());
	}

	/**
	 * ひらカタ漢字は全角に、ＡＢＣ１２３は半角に 変換する。
	 *
	 * @param value
	 * @return
	 */
	public static String transAnyNFKC(String value) {
		return transAnyNFKC.transliterate(value);
	}

	/**
	 * ひらがなをカタカナに置き換える。<br>
	 *
	 * @param value
	 * @return
	 */
	public static String transHiraToKana(String value) {
		return transHiraToKana.transliterate(value);
	}

	/**
	 * カタカナをひらがなに置き換える。<br>
	 *
	 * @param value
	 * @return
	 */
	public static String transKanaToHira(String value) {
		return transKanaToHira.transliterate(value);
	}

	/**
	 * 引数が空文字またはnullの場合、空文字に置き換えます。
	 *
	 * @param value
	 * @return
	 */
	public static String replaceEmpty(String value) {

		if (StringUtils.isEmpty(value)) {
			return "";
		}
		return value;
	}

	/**
	 * 第1引数が空文字またはnullでない場合、第2引数のリストに要素を追加する。
	 *
	 * @param str
	 * @param list
	 */
	public static void addList(String str, List<String> list) {
		if (!StringUtils.isEmpty(str)) {
			list.add(str);
		}
	}

	/**
	 * 第1引数が空文字またはnullでない場合、第3引数の編集後、第2引数のリストに要素を追加する。
	 *
	 * @param str
	 * @param list
	 * @param editFunc
	 */
	public static void addList(String str, List<String> list, Function<String, String> editFunc) {
		if (!StringUtils.isEmpty(str)) {
			list.add(editFunc.apply(str));
		}
	}

	/**
	 * resources配下に配置したYamlファイルを読み込み、ファイル内容を返却する。
	 * @param <T>
	 *
	 * @param fileName
	 * @return
	 * @throws IOException
	 */
	public static <T> T loadYaml(String fileName, Class<? extends T> clazz) throws IOException {

		Resource resource = loadFile(fileName);
		return convYaml(resource, clazz);
	}

	/**
	 * Yaml形式のResource型から、第2引数で指定した型に変換する。
	 *
	 * @param <T>
	 * @param resource
	 * @param clazz
	 * @return
	 * @throws IOException
	 */
	public static <T> T convYaml(Resource resource, Class<? extends T> clazz) throws IOException {

		InputStreamReader reader = new InputStreamReader(resource.getInputStream());

		Yaml yaml = new Yaml();
		return yaml.loadAs(reader, clazz);
	}

	/**
	 * resource配下に配置したファイルを読み込み、Resourceを返却する。
	 *
	 * @param fileName
	 * @return
	 */
	public static Resource loadFile(String fileName) {

		DefaultResourceLoader resourceLoader = new DefaultResourceLoader();
		return resourceLoader.getResource("classpath:" + fileName);
	}

    /**
     * Clonableが継承されていないクラスのインスタンスのディープコピーを作成したい時に使用する。
     * @param <T>
     *
     * @param original
     * @return
     */
    @SuppressWarnings("unchecked")
	public static <T extends Serializable> T deepCopy(T original) {
        try {
            // 1. オブジェクトをシリアライズしてバイトストリームに変換する
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(original);

            // 2. バイトストリームからオブジェクトをデシリアライズする
            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(bais);
            Object copy = ois.readObject();

            // 3. 新しいオブジェクトを返す
            return (T) copy;
        } catch (IOException | ClassNotFoundException e) {
            // 例外が発生した場合はnullを返す
            return null;
        }
    }

    /**
     * 四捨五入用メソッド</br>
     * 例：round(1, 4, 2) -> 0.3
     *
     * @param divisor 割られる数
     * @param divident 割る数
     * @param roundDigit 四捨五入する小数点以下桁
     * @return
     */
    public static float round(float divisor, float divident, int roundDigit) {

    	float num = (float) Math.pow(10.0, roundDigit - 1);
    	return Math.round((divisor / divident) * num) / num;
    }
}
