package jp.brainjuice.pokego.business.service.utils;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import jp.brainjuice.pokego.business.constant.Type.TypeEnum;
import jp.brainjuice.pokego.business.dao.entity.GoPokedex;

public class PokemonEditUtils {

	/** ノーマル */
	private static final String N = "N";
	/** ガラル地方 */
	private static final String G = "G";
	/** アローラ地方 */
	private static final String A = "A";
	/** ヒスイ地方 */
	private static final String H = "H";
	/** パルデア地方 */
	private static final String P = "P";
	/** メガシンカ */
	private static final String M = "M";

	/**
	 * 亜種コードの順序
	 */
	private static Map<String, Integer> subspeciesMap = new HashMap<String, Integer>();
	static {
		subspeciesMap.put(N, Integer.valueOf(0));
		subspeciesMap.put(A, Integer.valueOf(1));
		subspeciesMap.put(G, Integer.valueOf(2));
		subspeciesMap.put(H, Integer.valueOf(3));
		subspeciesMap.put(P, Integer.valueOf(4));
		subspeciesMap.put(M, Integer.valueOf(5));
	}

	/**
	 * 図鑑IDから図鑑№を取得する。
	 *
	 * @param pokedexId
	 * @return
	 */
	public static int getPokedexNo(String pokedexId) {
		return Integer.valueOf(getStrPokedexNo(pokedexId)).intValue();
	}

	/**
	 * 図鑑IDから図鑑№を取得する。
	 *
	 * @param pokedexId
	 * @return
	 */
	public static String getStrPokedexNo(String pokedexId) {
		return pokedexId.substring(0, 4);
	}

	/**
	 * 図鑑IDから亜種コードを取得する。
	 *
	 * @param pokedexId
	 * @return
	 */
	public static String getSubspecies(String pokedexId) {
		return pokedexId.substring(4, 5);
	}

	/**
	 * 図鑑IDから連番を取得する。
	 *
	 * @param pokedexId
	 * @return
	 */
	public static String getSerialNumber(String pokedexId) {
		return pokedexId.substring(5);
	}

	/**
	 * メガシンカのpokedexIdかどうかを判定する。
	 *
	 * @param pokedexId
	 * @return
	 */
	public static boolean isMega(String pokedexId) {
		return M.equals(getSubspecies(pokedexId));
	}

	/**
	 * メガシンカ前のポケモンのpokedexIdを取得する。
	 * 指定されたpokedexIdがメガシンカ後のポケモン出ない場合は、nullを返却する。
	 *
	 * TODO: リージョンフォームのポケモンがメガシンカするようになったら、考慮が必要。
	 *
	 * @param pokedexId
	 * @return
	 */
	public static String getPokedexIdBeforeMegaEvo(String pokedexId) {

		if (!isMega(pokedexId)) {
			return null;
		}

		String pokedexNo = getStrPokedexNo(pokedexId);

		// TODO: 連番は絶対に"01"である前提の仕様。
		return pokedexNo + N + "01";
	}

	/**
	 * メガシンカ、キョダイマックス等の特殊フォルムかどうかを判定する。
	 * TODO: キョダイマックス実装後に見直す。
	 *
	 * @param pokedexId
	 * @return
	 */
	public static boolean isSpecialForm(String pokedexId) {
		return isMega(pokedexId);
	}

	/**
	 * 図鑑IDから連番を取得する。
	 *
	 * @param pokedexId
	 * @return
	 */
	public static int getSerial(String pokedexId) {
		return Integer.valueOf(pokedexId.substring(5, 7)).intValue();
	}

	/**
	 * ポケモンの名前に備考を連結させた文字列を返却します。<br>
	 * 備考は括弧("(",")")で括ります。
	 *
	 * @param goPokedex
	 * @return
	 */
	public static String appendRemarks(GoPokedex goPokedex) {

		StringBuilder sb = new StringBuilder();
		sb.append(goPokedex.getName());

		String remarks = goPokedex.getRemarks();
		if (!StringUtils.isEmpty(remarks)) {
			// 備考がある場合は、"(" + 備考 + ")"を連結する。
			sb.append('(').append(remarks).append(')');
		}

		return sb.toString();
	}

	/**
	 * Optional型のタイプ英字名から、Optional型のTypeEnumを取得します。<br>
	 * nullの場合はOptional型のnullを返却します。
	 *
	 * @param type
	 * @return
	 */
	public static Optional<TypeEnum> convTypeEnum(Optional<String> type) {
		return type.isPresent() ? Optional.of(TypeEnum.valueOf(type.get())) : Optional.empty();
	}

	/**
	 * 図鑑IDを並び替える用のComparatorを取得します。
	 *
	 * @return
	 */
	public static Comparator<String> getPokedexIdComparator() {

		return (o1, o2) -> {
			// 図鑑№の昇順
			final int pokedexNo1 = getPokedexNo(o1);
			final int pokedexNo2 = getPokedexNo(o2);
			if (pokedexNo1 < pokedexNo2) return -1;
			if (pokedexNo1 > pokedexNo2) return 1;

			// 亜種コードの昇順
			final String subspecies1 = getSubspecies(o1);
			final String subspecies2 = getSubspecies(o2);
			if (subspeciesMap.get(subspecies1) < subspeciesMap.get(subspecies2)) return -1;
			if (subspeciesMap.get(subspecies1) > subspeciesMap.get(subspecies2)) return 1;

			// 連番の昇順
			return getSerial(o1) - getSerial(o2);
		};
	}

	/**
	 * 図鑑IDを並び替える用のComparatorを取得します。
	 *
	 * @param order 正の数の場合：昇順、負の数の場合：降順
	 * @return
	 */
	public static Comparator<GoPokedex> getPokedexComparator(int order) {

		return (o1, o2) -> {
			// 図鑑№の昇順
			final int pokedexNo1 = getPokedexNo(o1.getPokedexId());
			final int pokedexNo2 = getPokedexNo(o2.getPokedexId());
			if (pokedexNo1 < pokedexNo2) return order > 0 ? -1 : 1;
			if (pokedexNo1 > pokedexNo2) return order > 0 ? 1 : -1;

			// 亜種コードの昇順
			final String subspecies1 = getSubspecies(o1.getPokedexId());
			final String subspecies2 = getSubspecies(o2.getPokedexId());
			if (subspeciesMap.get(subspecies1) < subspeciesMap.get(subspecies2)) return order > 0 ? -1 : 1;
			if (subspeciesMap.get(subspecies1) > subspeciesMap.get(subspecies2)) return order > 0 ? 1 : -1;

			// 連番の昇順
			return getSerial(o1.getPokedexId()) - getSerial(o2.getPokedexId());
		};
	}

	/**
	 * String型の値を取得します。enum型、String型に対応しています。
	 *
	 * @param value
	 * @return
	 */
	public static String getStrName(Object value) {
		return value.getClass().isEnum() ? ((Enum<?>) value).name() : (String) value;
	}
}
