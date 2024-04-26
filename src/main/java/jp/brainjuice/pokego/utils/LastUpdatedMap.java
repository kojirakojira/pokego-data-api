package jp.brainjuice.pokego.utils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.ibm.icu.text.MessageFormat;

import jp.brainjuice.pokego.utils.LastUpdatedMap.Keys;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class LastUpdatedMap extends HashMap<Keys, Date> {

	private static final String LAST_UPDATED_FILE_NAME = "last-updated.yml";

	/**
	 * last-updated.ymlで使用するキー名
	 *
	 * @author saibabanagchampa
	 */
	public enum Keys {
		unimplPokemon // 未実装ポケモン一覧
	}

	/**
	 * コンストラクタ
	 */
	public LastUpdatedMap() {

		try {
			// last-updated.ymlを読み込み、Mapに格納する。
			@SuppressWarnings("unchecked")
			Map<Keys, Date> dateMap = ((Map<String, String>) BjUtils.loadYaml(LAST_UPDATED_FILE_NAME, Map.class)).entrySet().stream()
					.collect(Collectors.toMap(
							entry -> Keys.valueOf(entry.getKey()),
							entry -> BjUtils.parseDate(entry.getValue(), BjUtils.sdfYmd)));
			putAll(dateMap);

			log.info(MessageFormat.format("LastUpdated generated!! (Referenced file: resources/{0})", LAST_UPDATED_FILE_NAME));
		} catch (Exception e) {
			// 初期化失敗した場合は、RuntimeExceptionとして処理する。後続処理の実行は不可能とする。
			throw new RuntimeException(e);
		}
	}

	/**
	 * 日付を取得する。
	 *
	 * @param key
	 * @param sdf
	 * @return
	 */
	public String get(Keys key, String sdf) {
		return BjUtils.formatDate(get(key), sdf);
	}

}
