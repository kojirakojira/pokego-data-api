package jp.brainjuice.pokego.business.service.utils;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

import jp.brainjuice.pokego.business.constant.Type.TypeEnum;
import jp.brainjuice.pokego.business.service.utils.dto.type.TwoTypeKey;

public class TypeUtils {


	/**
	 * TwoTypeKeyのStreamを取得する。
	 * ※1タイプのTwoTypeKeyは含まない。
	 *
	 * @return
	 */
	public static Stream<TwoTypeKey> getTwoTypeStream() {
		return Stream.of(TypeEnum.values())
				.flatMap(te -> Stream.of(TypeEnum.values()).map(te2 -> new TwoTypeKey(te, te2))) // TwoTypeKeyを作成。
				.distinct() // 重複除去
				.filter(ttk -> ttk.getType1() != ttk.getType2()); // タイプ1,2が重複しているものは省く。
	}

	/**
	 * TwoTypeKeyのStreamを取得する。
	 * ※1タイプのTwoTypeKeyも含む。1タイプの場合はタイプ2がnullになる。
	 *
	 * @return
	 */
	public static Stream<TwoTypeKey> getTwoTypeStreamContainsOneType() {
		return Stream.of(TypeEnum.values())
				.flatMap(te -> Stream.of(TypeEnum.values()).map(te2 -> new TwoTypeKey(te, te2))) // TwoTypeKeyを作成。
				.distinct() // 重複除去
				.map(ttk -> {
					if (ttk.getType1() == ttk.getType2()) {
						ttk.setType2(null);
					}
					return ttk;
				}); // タイプ1,2が重複している場合は、単一タイプとし、タイプ2を除去する。
	}

	/**
	 * TwoTypeKeyのStreamを取得する。
	 * ※1タイプのみを表現するTwoTypeKey
	 *
	 * @return
	 */
	public static Stream<TwoTypeKey> getOneTypeTwoTypeStream() {
		return Stream.of(TypeEnum.values())
				.map(te -> new TwoTypeKey(te, null));
	}

	/**
	 * TypeEnum型のタイプリストを、文字列へ変換する。
	 *
	 * @param typeList
	 * @param func
	 * @param separator
	 * @return
	 */
	public static String joinType(
			List<TypeEnum> typeList,
			Function<TypeEnum, String> typeConverter,
			String separator) {

		if (typeList == null) return "";

		return StringUtils.join(
				typeList.stream().map(typeConverter).collect(Collectors.toList()),
				separator);
	}
}
