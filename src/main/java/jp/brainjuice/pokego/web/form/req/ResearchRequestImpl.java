package jp.brainjuice.pokego.web.form.req;

import java.util.List;

import jp.brainjuice.pokego.business.constant.GenNameEnum;
import jp.brainjuice.pokego.business.constant.RegionEnum;
import jp.brainjuice.pokego.business.constant.Type.TypeEnum;
import lombok.Data;

@Data
public abstract class ResearchRequestImpl implements ResearchRequest {

	/** idで検索する場合 */
	private String id;
	/** nameで検索する場合 */
	private String name;

	/** 最終進化 */
	private boolean finEvo;
	private boolean negaFinEvo;
	/** メガシンカ */
	private boolean mega;
	private boolean negaMega;
	/** 実装済み */
	private boolean impled;
	private boolean negaImpled;
	/** 強ポケ補正 */
	private boolean tooStrong;
	private boolean negaTooStrong;
	/**
	 * 地域
	 * @see RegionEnum
	 */
	private List<String> region;
	private boolean negaRegion;
	/**
	 * タイプ１
	 * @see TypeEnum
	 */
	private String type1;
	/**
	 * タイプ２
	 * @see TypeEnum
	 */
	private String type2;
	/**
	 * 世代
	 * @see GenNameEnum
	  */
	private List<String> gen;
	private boolean negaGen;


	/**
	 * 閲覧数カウントをオフにする
	 */
	private boolean enableCount;
}
