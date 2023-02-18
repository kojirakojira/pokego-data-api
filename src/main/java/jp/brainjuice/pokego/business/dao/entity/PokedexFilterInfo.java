package jp.brainjuice.pokego.business.dao.entity;

import java.io.Serializable;

import jp.brainjuice.pokego.business.constant.GenNameEnum;
import jp.brainjuice.pokego.business.constant.RegionEnum;
import jp.brainjuice.pokego.business.constant.Type.TypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class PokedexFilterInfo implements Serializable, Cloneable {

	/** 図鑑No(4) + 亜種コード(1) + 連番(2) */
	private String pokedexId;
	/** タイプ１ */
	private TypeEnum type1;
	/** タイプ２ */
	private TypeEnum type2;
	/** 最終進化 */
	private boolean finalEvo;
	/** メガシンカ */
	private boolean mega;
	/** 実装済み */
	private boolean impled;
	/** 強ポケ補正 */
	private boolean tooStrong;
	/** 地域 */
	private RegionEnum region;
	/** 世代 */
	private GenNameEnum gen;

	public PokedexFilterInfo clone() {
		PokedexFilterInfo filterInfo = null;
		try {
			filterInfo = (PokedexFilterInfo) super.clone();
		} catch (CloneNotSupportedException e) {
			log.error("Clone failed.", e);
		}
		return filterInfo;
	}

	public String getRegionEnum() {
		return getRegion().getCode();
	}

	public String getType1Name() {
		return getType1().name();
	}

	public String getType2Name() {
		// タイプ2はnullがあり得る。
		return getType2() == null ? null : getType2().name();
	}
}
