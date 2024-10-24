package jp.brainjuice.pokego.business.dao.entity;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jp.brainjuice.pokego.business.constant.GenNameEnum;
import jp.brainjuice.pokego.business.constant.Type.TypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * GOポケモン図鑑
 *
 * @author saibabanagchampa
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@ToString
@Table(name = "go_pokedex")
public class GoPokedex implements Serializable {

	/** 図鑑No(4) + 亜種コード(1) + 連番(2) */
	@Id
	@Column(name = "pokedex_id", nullable = false, columnDefinition = "bpchar")
	private String pokedexId;

	/** ポケモン */
	@Column(nullable = false, length = 20)
	private String name;

	/** こうげき */
	@Column(nullable = false)
	private int attack;

	/** ぼうぎょ */
	@Column(nullable = false)
	private int defense;

	/** HP */
	@Column(nullable = false)
	private int hp;

	/** 備考 */
	@Column(nullable = false, length = 256)
	private String remarks;

	/** タイプ１ */
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 10)
	private TypeEnum type1;

	/** タイプ２ */
	@Enumerated(EnumType.STRING)
	@Column(length = 10)
	private TypeEnum type2;

	/** 世代 */
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 16)
	private GenNameEnum gen;

	/** 画像1（アバター用） */
	@Column(length = 256)
	private String image1;

	/** 画像2（サムネイル用） */
	@Column(length = 256)
	private String image2;

	/** 実装フラグ */
	@Column(name = "impl_flg", nullable = false)
	private boolean implFlg;

	/** リージョン、メガ（図鑑IDの5桁目） */
	@Column(nullable = false, columnDefinition = "bpchar")
	private String region;

	/** 強ポケ補正対象か否か */
	@Column(name = "too_strong", nullable = false)
	private boolean tooStrong;

	/** 最終進化か否か */
	@Column(name = "fin_evo", nullable = false)
	private boolean finEvo;

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
