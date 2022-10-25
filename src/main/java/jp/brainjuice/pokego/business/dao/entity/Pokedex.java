package jp.brainjuice.pokego.business.dao.entity;

import java.io.Serializable;

import javax.annotation.Nonnull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class Pokedex implements Serializable, Cloneable {

	/** 図鑑No(4) + 亜種フラグ(1) + 連番(2) */
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

	/** タイプ１ */
	@Nonnull
	private String type1;

	/** タイプ２ */
	@Nonnull
	private String type2;

	/** 実装フラグ */
	@Nonnull
	private boolean implFlg;

	/** 備考 */
	private String remarks;

	public Pokedex clone() {
		Pokedex pokedex = null;
		try {
			pokedex = (Pokedex) super.clone();
		} catch (CloneNotSupportedException e) {
			log.error("Clone failed.");
		}
		return pokedex;
	}
}
