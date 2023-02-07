package jp.brainjuice.pokego.business.dao.entity;

import java.io.Serializable;

import jp.brainjuice.pokego.business.service.utils.PokemonEditUtils.RegionEnum;
import jp.brainjuice.pokego.business.service.utils.memory.TypeMap.TypeEnum;
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
	/** 最終進化 */
	private boolean finalEvo;
	/** メガシンカ */
	private boolean mega;
	/** 実装済み */
	private boolean impled;
	/** 地域 */
	private RegionEnum region;
	/** タイプ１ */
	private TypeEnum type1;
	/** タイプ２ */
	private TypeEnum type2;

	public PokedexFilterInfo clone() {
		PokedexFilterInfo filterInfo = null;
		try {
			filterInfo = (PokedexFilterInfo) super.clone();
		} catch (CloneNotSupportedException e) {
			log.error("Clone failed.", e);
		}
		return filterInfo;
	}
}
