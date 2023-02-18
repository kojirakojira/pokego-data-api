package jp.brainjuice.pokego.web.form.req.research;

import java.util.List;

/**
 * ResearchSearviceExecutorを使用してポケモン情報の検索をする場合に継承するインタフェースです。<br>
 * 「nega」がつくメソッドはその項目の否定形で検索する場合に指定する項目。
 *
 * @author saibabanagchampa
 *
 */
public interface ResearchRequest {

	public String getId();
	public void setId(String id);

	public String getName();
	public void setName(String name);

	/** タイプ１ */
	public String getType1();
	public void setType1(String type1);

	/** タイプ２ */
	public String getType2();
	public void setType2(String type2);

	/** 最終進化 */
	public boolean isFinEvo();
	public void setFinEvo(boolean finEvo);
	public boolean isNegaFinEvo();
	public void setNegaFinEvo(boolean negaFinEvo);

	/** メガシンカ */
	public boolean isMega();
	public void setMega(boolean mega);
	public boolean isNegaMega();
	public void setNegaMega(boolean negaMega);

	/** 実装済み */
	public boolean isImpled();
	public void setImpled(boolean impled);
	public boolean isNegaImpled();
	public void setNegaImpled(boolean negaImpled);

	/** 強ポケ補正 */
	public boolean isTooStrong();
	public void setTooStrong(boolean tooStrong);
	public boolean isNegaTooStrong();
	public void setNegaTooStrong(boolean negaTooStrong);

	/** 地域 */
	public List<String> getRegion();
	public void setRegion(List<String> region);
	public boolean isNegaRegion();
	public void setNegaRegion(boolean negaRegion);

	/** 世代 */
	public List<String> getGen();
	public void setGen(List<String> gen);
	public boolean isNegaGen();
	public void setNegaGen(boolean negaGen);

}
