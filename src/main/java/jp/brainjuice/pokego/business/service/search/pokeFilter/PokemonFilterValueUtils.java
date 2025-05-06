package jp.brainjuice.pokego.business.service.search.pokeFilter;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import jp.brainjuice.pokego.business.constant.ConstantEnumInterface;
import jp.brainjuice.pokego.business.constant.GenNameEnum;
import jp.brainjuice.pokego.business.constant.RegionEnum;
import jp.brainjuice.pokego.business.constant.Type.TypeEnum;
import jp.brainjuice.pokego.business.service.search.pokeFilter.dto.SearchValue;
import jp.brainjuice.pokego.business.service.search.utils.PokemonEditUtils;
import jp.brainjuice.pokego.dao.jpa.GoPokedexSpecifications;
import jp.brainjuice.pokego.dao.jpa.dto.FilterParam;
import jp.brainjuice.pokego.web.search.form.res.elem.DispFilterParam;

/**
 * ポケモンの絞り込み機能を使用するためのユーティリティクラス
 */
public class PokemonFilterValueUtils {

	/**
	 * リクエストから取得した絞り込み用の検索値をPokedexFilterInfoRepositoryで検索する用のマップに変換する。
	 *
	 * @param filterValue
	 * @return
	 * @see GoPokedexSpecifications
	 * @see SearchValue
	 * @see PokemonFilterValue
	 */
	public static Map<FilterEnum, FilterParam> mapping(PokemonFilterValue fv) {

		// 並び順を保持する。
		Map<FilterEnum, FilterParam> retMap = new LinkedHashMap<>();
		// タイプ（画面ではタイプ1、タイプ2の2つのみ指定できる仕様）
		TypeEnum t1 = fv.getType1();
		TypeEnum t2 = fv.getType2();
		if (t1 != null && t2 != null) {
			retMap.put(FilterEnum.twoType, new FilterParam(Arrays.asList(t1, t2)));
		} else if (t1 != null) {
			retMap.put(FilterEnum.type, new FilterParam(t1));
		} else if (t2 != null) {
			retMap.put(FilterEnum.type, new FilterParam(t2));
		}

		// 最終進化
		if (fv.isFinalEvo()) {
			retMap.put(FilterEnum.finEvo, new FilterParam(fv.isFinalEvo(), fv.isNegaFinalEvo()));
		}
		// メガシンカ
		if (fv.isMega()) {
			retMap.put(FilterEnum.mega, new FilterParam(fv.isMega(), fv.isNegaMega()));
		}
		// ダイマックス
		if (fv.isDynamax()) {
			retMap.put(FilterEnum.dynamax, new FilterParam(fv.isDynamax(), fv.isNegaDynamax()));
		}
		// キョダイマックス
		if (fv.isGigantamax()) {
			retMap.put(FilterEnum.gigantamax, new FilterParam(fv.isGigantamax(), fv.isNegaGigantamax()));
		}
		// 実装済み
		if (fv.isImpled()) {
			retMap.put(FilterEnum.impled, new FilterParam(fv.isImpled(), fv.isNegaImpled()));
		}
		// 強ポケ補正
		if (fv.isTooStrong()) {
			retMap.put(FilterEnum.tooStrong, new FilterParam(fv.isTooStrong(), fv.isNegaTooStrong()));
		}
		// 地域
		if (fv.getRegionList() != null && !fv.getRegionList().isEmpty()) {
			retMap.put(FilterEnum.region, new FilterParam(fv.getRegionList(), fv.isNegaRegion()));
		}

		// 世代
		if (fv.getGenList() != null && !fv.getGenList().isEmpty()) {
			retMap.put(FilterEnum.gen, new FilterParam(fv.getGenList(), fv.isNegaGen()));
		}


		return retMap;
	}

	/**
	 * 絞り込み検索値を画面表示用に変換する。
	 *
	 * @param filterMap
	 * @return
	 */
	public static List<DispFilterParam> convDisp(Map<FilterEnum, FilterParam> filterMap) {

		List<DispFilterParam> retList = filterMap.entrySet().stream().map(entry -> {
			DispFilterParam dfp = new DispFilterParam();
			FilterEnum key = entry.getKey();
			Object value = entry.getValue().getFilterValue();
			boolean negate = entry.getValue().isNegate();

			// nameのput
			dfp.setName(key.getJpn());

			// filterValueのput
			String filterValue = "";
			switch (key) {
			case type:
			case twoType: {
				// タイプ
				filterValue = editNegateStr(getStrValue(value, TypeEnum.class), negate);
				break;
			}
			case finEvo:
			case mega:
			case dynamax:
			case gigantamax:
			case impled:
			case tooStrong:
				// 最終進化、メガシンカ、ダイマックス、キョダイマックス、実装済み、強ポケ補正
				filterValue = (boolean) value && negate ? "否定による絞り込み" : "絞り込む";
				break;

			case region: {
				filterValue = editNegateStr(getStrValue(value, RegionEnum.class), negate);
				break;
			}
			case gen: {
				filterValue = editNegateStr(getStrValue(value, GenNameEnum.class), negate);
				break;
			}
			}
			dfp.setFilterValue(filterValue);

			// negateのput
			if (negate) {
				dfp.setNegate("する");
			}

			return dfp;
		}).collect(Collectors.toList());

		return retList;
	}

	/**
	 * String型で値を取得する。カンマ区切り。
	 *
	 * @param <E>
	 * @param value
	 * @param clazz
	 * @return
	 */
	private static <E extends Enum<E> & ConstantEnumInterface> String getStrValue(Object value, Class<E> clazz) {

		String ret = "";
		if (value instanceof List) {
			// Listの場合
			List<String> vList = ((List<?>) value).stream().map((v) ->
			Enum.valueOf(clazz, PokemonEditUtils.getStrName(v)).getJpn()).collect(Collectors.toList());
			ret = StringUtils.join(vList, ", ");

		} else if (value instanceof Enum || value instanceof String) {
			// EnumまたはString型の場合
			ret = Enum.valueOf(clazz, PokemonEditUtils.getStrName(value)).getJpn();
		}

		return ret;
	}

	/**
	 * @param str
	 * @param negate
	 * @return
	 */
	private static String editNegateStr(String str, boolean negate) {
		return negate ? MessageFormat.format("{0} 以外", str) : str;
	}
}
