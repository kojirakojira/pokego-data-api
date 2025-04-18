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

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Table(name = "pokedex")
public class Pokedex implements Serializable {

	/** 図鑑No(4) + 亜種コード(1) + 連番(2) */
	@Id
	@Column(name = "pokedex_id", nullable = false, columnDefinition = "bpchar")
	private String pokedexId;

	/** ポケモン */
	@Column(nullable = false, length = 20)
	private String name;

	/** HP */
	@Column(nullable = false)
	private int hp;

	/** こうげき */
	@Column(nullable = false)
	private int attack;

	/** ぼうぎょ */
	@Column(nullable = false)
	private int defense;

	/** とくこう */
	@Column(name = "special_attack", nullable = false)
	private int specialAttack;

	/** とくぼう */
	@Column(name = "special_defense", nullable = false)
	private int specialDefense;

	/** すばやさ */
	@Column(nullable = false)
	private int speed;

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
	@Column(name = "impl_flg", nullable = false, length = 20)
	private boolean implFlg;

	/** 実装フラグ */
	@Column(name = "pre_mega_pokedex_id", columnDefinition = "bpchar")
	private String preMegaPokedexId;

}
