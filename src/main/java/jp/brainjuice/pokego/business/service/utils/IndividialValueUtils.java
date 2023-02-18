package jp.brainjuice.pokego.business.service.utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import jp.brainjuice.pokego.business.constant.GenNameEnum;
import jp.brainjuice.pokego.business.constant.RegionEnum;
import jp.brainjuice.pokego.business.constant.Type.TypeEnum;
import jp.brainjuice.pokego.business.dao.PokedexFilterInfoRepository;
import jp.brainjuice.pokego.business.dao.PokedexFilterInfoRepository.FilterEnum;
import jp.brainjuice.pokego.business.dao.dto.FilterParam;
import jp.brainjuice.pokego.business.service.utils.dto.IndividialValue;
import jp.brainjuice.pokego.business.service.utils.dto.PokemonFilterValue;
import jp.brainjuice.pokego.web.form.req.research.ResearchRequest;

public class IndividialValueUtils {

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

		Map<FilterEnum, FilterParam> retMap = new HashMap<>();
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
			retMap.put(FilterEnum.finalEvo, new FilterParam(fv.isFinalEvo(), fv.isNegaFinalEvo()));
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
			retMap.put(FilterEnum.TooStrong, new FilterParam(fv.isTooStrong(), fv.isNegaTooStrong()));
		}
		// 地域
		if (fv.getRegionList() != null && fv.getRegionList().isEmpty()) {
			retMap.put(FilterEnum.region, new FilterParam(fv.getRegionList(), fv.isNegaRegion()));
		}

		// 世代
		if (fv.getGenList() != null && !fv.getGenList().isEmpty()) {
			retMap.put(FilterEnum.gen, new FilterParam(fv.getGenList(), fv.isNegaGen()));
		}


		return retMap;
	}

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
}
