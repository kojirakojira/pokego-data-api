package jp.brainjuice.pokego.business.service.utils;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class PokemonEditUtils {

	private static final String M = "M";
	private static final String N = "N";
	private static final String G = "G";
	private static final String A = "A";
	private static final String H = "H";

	/**
	 * 亜種コードの順序
	 */
	private static Map<String, Integer> subspeciesMap = new HashMap<String, Integer>();
	{
		subspeciesMap.put(M, Integer.valueOf(0));
		subspeciesMap.put(N, Integer.valueOf(1));
		subspeciesMap.put(A, Integer.valueOf(2));
		subspeciesMap.put(G, Integer.valueOf(3));
		subspeciesMap.put(H, Integer.valueOf(4));
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
	 * メガシンカのpokedexIdかどうかを判定する。
	 *
	 * @param pokedexId
	 * @return
	 */
	public static boolean isMega(String pokedexId) {
		return M.equals(getSubspecies(pokedexId));
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
	 * String型の値を取得します。enum型、String型に対応しています。
	 *
	 * @param value
	 * @return
	 */
	public static String getStrName(Object value) {
		return value.getClass().isEnum() ? ((Enum<?>) value).name() : (String) value;
	}
}
