package jp.brainjuice.pokego.business.service.utils;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import jp.brainjuice.pokego.business.constant.ConstantEnum;
import jp.brainjuice.pokego.business.constant.GenNameEnum;
import jp.brainjuice.pokego.business.constant.RegionEnum;
import jp.brainjuice.pokego.business.constant.Type.TypeEnum;
import jp.brainjuice.pokego.business.dao.PokedexFilterInfoRepository;
import jp.brainjuice.pokego.business.dao.PokedexFilterInfoRepository.FilterEnum;
import jp.brainjuice.pokego.business.dao.dto.FilterParam;
import jp.brainjuice.pokego.business.service.utils.dto.IndividialValue;
import jp.brainjuice.pokego.business.service.utils.dto.PokemonFilterValue;
import jp.brainjuice.pokego.web.form.req.research.ResearchRequest;

public class PokemonFilterValueUtils {

	private static final String DISP_NAME = "name";
	private static final String DISP_FILTER_VALUE = "filterValue";
	private static final String DISP_NEGATE = "negate";

	/**
	 * PokemonFilterValueを生成する。
	 *
	 * @param req
	 * @return
	 */
	public static PokemonFilterValue createPokemonFilterValue(ResearchRequest req) {

		PokemonFilterValue filterValue = new PokemonFilterValue();
		// タイプ１
		filterValue.setType1(
				req.getType1() == null ? null : TypeEnum.valueOf(req.getType1()));
		// タイプ２
		filterValue.setType2(
				req.getType2() == null ? null : TypeEnum.valueOf(req.getType2()));
		// 最終進化
		filterValue.setFinalEvo(req.isFinEvo());
		filterValue.setNegaFinalEvo(req.isNegaFinEvo());
		// メガシンカ
		filterValue.setMega(req.isMega());
		filterValue.setNegaMega(req.isNegaMega());
		// 実装済み
		filterValue.setImpled(req.isImpled());
		filterValue.setNegaImpled(req.isNegaImpled());
		// 強ポケ補正
		filterValue.setTooStrong(req.isTooStrong());
		filterValue.setNegaTooStrong(req.isNegaTooStrong());
		// 地域
		filterValue.setRegionList(
				req.getRegion() == null ? null : req.getRegion().stream().map(RegionEnum::valueOf).collect(Collectors.toList()));
		filterValue.setNegaRegion(req.isNegaRegion());
		// 世代
		filterValue.setGenList(
				req.getGen() == null ? null : req.getGen().stream().map(GenNameEnum::valueOf).collect(Collectors.toList()));
		filterValue.setNegaGen(req.isNegaGen());

		return filterValue;
	}

	/**
	 * リクエストから取得した絞り込み用の検索値をPokedexFilterInfoRepositoryで検索する用のマップに変換する。
	 *
	 * @param filterValue
	 * @return
	 * @see PokedexFilterInfoRepository
	 * @see IndividialValue
	 * @see PokemonFilterValue
	 */
	public static Map<FilterEnum, FilterParam> mapping(PokemonFilterValue fv) {

		// 並び順を保持する。
		Map<FilterEnum, FilterParam> retMap = new LinkedHashMap<>();
		// タイプ
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
	public static List<Map<String, String>> convDisp(Map<FilterEnum, FilterParam> filterMap) {

		List<Map<String, String>> retList = filterMap.entrySet().stream().map(entry -> {
			Map<String, String> map = new HashMap<>();
			FilterEnum key = entry.getKey();
			Object value = entry.getValue().getFilterValue();
			boolean negate = entry.getValue().isNegate();

			// nameのput
			map.put(DISP_NAME, key.getJpn());

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
			case impled:
			case tooStrong:
				// 最終進化、メガシンカ、実装済み、強ポケ補正
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
			map.put(DISP_FILTER_VALUE, filterValue);

			// negateのput
			if (negate) {
				map.put(DISP_NEGATE, "する");
			}

			return map;
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
	private static <E extends Enum<E> & ConstantEnum> String getStrValue(Object value, Class<E> clazz) {

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
