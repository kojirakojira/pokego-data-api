package jp.brainjuice.pokego.business.service.pokeFilter;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import jp.brainjuice.pokego.business.constant.GenNameEnum;
import jp.brainjuice.pokego.business.constant.RegionEnum;
import jp.brainjuice.pokego.business.constant.Type.TypeEnum;
import jp.brainjuice.pokego.web.form.req.ResearchRequest;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 絞り込み用の検索値
 *
 * @author saibabanagchampa
 *
 */
@Data
@NoArgsConstructor
public class PokemonFilterValue {

	/** 最終進化 */
	private boolean finalEvo;
	private boolean negaFinalEvo;
	/** メガシンカ */
	private boolean mega;
	private boolean negaMega;
	/** ダイマックス */
	private boolean dynamax;
	private boolean negaDynamax;
	/** キョダイマックス */
	private boolean gigantamax;
	private boolean negaGigantamax;
	/** 実装済み */
	private boolean impled;
	private boolean negaImpled;
	/** 強ポケ補正 */
	private boolean tooStrong;
	private boolean negaTooStrong;
	/** 地域 */
	private List<RegionEnum> regionList;
	private boolean negaRegion;
	/** タイプ１ */
	private TypeEnum type1;
	/** タイプ２ */
	private TypeEnum type2;
	/** 世代 */
	private List<GenNameEnum> genList;
	private boolean negaGen;

	public PokemonFilterValue(ResearchRequest req) {

		// タイプ１
		setType1(
				StringUtils.isEmpty(req.getType1()) ? null : TypeEnum.valueOf(req.getType1()));
		// タイプ２
		setType2(
				StringUtils.isEmpty(req.getType2()) ? null : TypeEnum.valueOf(req.getType2()));
		// 最終進化
		setFinalEvo(req.isFinEvo());
		setNegaFinalEvo(req.isNegaFinEvo());
		// メガシンカ
		setMega(req.isMega());
		setNegaMega(req.isNegaMega());
		// ダイマックス
		setDynamax(req.isDynamax());
		setNegaDynamax(req.isNegaDynamax());
		// キョダイマックス
		setGigantamax(req.isGigantamax());
		setNegaGigantamax(req.isNegaGigantamax());
		// 実装済み
		setImpled(req.isImpled());
		setNegaImpled(req.isNegaImpled());
		// 強ポケ補正
		setTooStrong(req.isTooStrong());
		setNegaTooStrong(req.isNegaTooStrong());
		// 地域
		setRegionList(
				req.getRegion() == null ? null : req.getRegion().stream().map(RegionEnum::valueOf).collect(Collectors.toList()));
		setNegaRegion(req.isNegaRegion());
		// 世代
		setGenList(
				req.getGen() == null ? null : req.getGen().stream().map(GenNameEnum::valueOf).collect(Collectors.toList()));
		setNegaGen(req.isNegaGen());
	}
}