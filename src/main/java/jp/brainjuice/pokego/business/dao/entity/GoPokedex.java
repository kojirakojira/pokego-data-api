package jp.brainjuice.pokego.business.dao.entity;

import javax.annotation.Nonnull;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * GOポケモン図鑑
 *
 * @author saibabanagchampa
 *
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class GoPokedex extends Entity {

	/** 図鑑No(4) + 亜種コード(1) + 連番(2) */
	@Nonnull
	private String pokedexId;

	/** ポケモン */
	@Nonnull
	private String name;

	/** こうげき */
	@Nonnull
	private int attack;

	/** ぼうぎょ */
	@Nonnull
	private int defense;

	/** HP */
	@Nonnull
	private int hp;

	/** 備考 */
	@Nonnull
	private String remarks;

	/** タイプ１ */
	@Nonnull
	private String type1;

	/** タイプ２ */
	@Nonnull
	private String type2;

	/** 世代 */
	@Nonnull
	private String gen;

	/** 画像1（アバター用） */
	@Nonnull
	private String image1;

	/** 画像2（サムネイル用） */
	@Nonnull
	private String image2;

	/** 実装フラグ */
	@Nonnull
	private boolean implFlg;

	/**
	 * (非 Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
        return pokedexId.hashCode();
	}

    /**
     * (非 Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {

    	if (this == obj) {
    		return true;
    	}

    	if (!(obj instanceof GoPokedex)) {
    		return false;
    	}

    	GoPokedex other = (GoPokedex) obj;

    	return pokedexId != null && pokedexId.equals(other.getPokedexId());
    }
}
