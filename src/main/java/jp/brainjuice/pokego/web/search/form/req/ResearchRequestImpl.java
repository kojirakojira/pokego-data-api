package jp.brainjuice.pokego.web.search.form.req;

import java.util.List;

import jp.brainjuice.pokego.business.constant.GenNameEnum;
import jp.brainjuice.pokego.business.constant.RegionEnum;
import jp.brainjuice.pokego.business.constant.Type.TypeEnum;
import lombok.Data;

@Data
public abstract class ResearchRequestImpl implements ResearchRequest {

	/** idで検索する場合 */
	private String pid;
	/** nameで検索する場合 */
	private String name;

	/** 最終進化 */
	private boolean finEvo;
	private boolean negaFinEvo;
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
	/** リリース年月 */
	private int releaseDateStart;
	private int releaseDateEnd;
	/** 強ポケ補正 */
	private boolean tooStrong;
	private boolean negaTooStrong;
	/**
	 * 地域
	 * 
	 * @see RegionEnum
	 */
	private List<String> region;
	private boolean negaRegion;
	/**
	 * タイプ１
	 * 
	 * @see TypeEnum
	 */
	private String type1;
	/**
	 * タイプ２
	 * 
	 * @see TypeEnum
	 */
	private String type2;
	/**
	 * 世代
	 * 
	 * @see GenNameEnum
	 */
	private List<String> gen;
	private boolean negaGen;

	/**
	 * 閲覧数カウントをオフにする
	 */
	private boolean enableCount;
}
