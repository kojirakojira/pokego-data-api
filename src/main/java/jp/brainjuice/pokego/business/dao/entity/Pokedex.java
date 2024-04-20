package jp.brainjuice.pokego.business.dao.entity;

import javax.annotation.Nonnull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper=false)
@NoArgsConstructor
@AllArgsConstructor
public class Pokedex extends Entity {

	/** 図鑑No(4) + 亜種コード(1) + 連番(2) */
	private String pokedexId;

	/** ポケモン */
	@Nonnull
	private String name;

	/** HP */
	@Nonnull
	private int hp;

	/** こうげき */
	@Nonnull
	private int attack;

	/** ぼうぎょ */
	@Nonnull
	private int defense;

	/** とくこう */
	@Nonnull
	private int specialAttack;

	/** とくぼう */
	@Nonnull
	private int specialDefense;

	/** すばやさ */
	@Nonnull
	private int speed;

	/** 備考 */
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

	/** 実装フラグ */
	@Nonnull
	private boolean implFlg;

}
