package jp.brainjuice.pokego.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import jp.brainjuice.pokego.filter.log.LogUtils;

@Component
public final class BjUtils {

	public static final Locale locale = Locale.JAPAN;

	public static final String sdfYmdhme = "yyyy/MM/dd(E) HH:mm";
	public static final String sdfYmdhms = "yyyy-MM-dd HH:mm:ss";
	public static final String sdfYmdhm = "yyyy/MM/dd HH:mm";
	public static final String sdfYmd = "yyyy/MM/dd";
	public static final String sdfMde = "MM/dd(E)";
	public static final String sdfHm = "HH:mm";
	public static final String dirFormat = "yyyyMMddHHmmss";

//	private BjConfigMap bjdConfigMap;
//
//	@Autowired
//	public BjUtils(BjConfigMap bjdConfigMap) {
//		this.bjdConfigMap = bjdConfigMap;
//	}

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
}
