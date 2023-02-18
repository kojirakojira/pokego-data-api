package jp.brainjuice.pokego.business.service.utils.dto;

import java.util.List;

import jp.brainjuice.pokego.business.constant.GenNameEnum;
import jp.brainjuice.pokego.business.constant.RegionEnum;
import jp.brainjuice.pokego.business.constant.Type.TypeEnum;
import lombok.Data;

/**
 * 絞り込み用の検索値
 *
 * @author saibabanagchampa
 *
 */
@Data
public class PokemonFilterValue {

	/** 最終進化 */
	private boolean finalEvo;
	private boolean negaFinalEvo;
	/** メガシンカ */
	private boolean mega;
	private boolean negaMega;
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
}