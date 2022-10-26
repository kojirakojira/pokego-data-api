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
public class GoPokedex implements Serializable, Cloneable {

	/** 図鑑No(4) + 亜種フラグ(1) + 連番(2) */
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

	/** 画像 */
	private String image;

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

	/** 実装フラグ */
	@Nonnull
	private boolean implFlg;

	public GoPokedex clone() {
		GoPokedex goPokedex = null;
		try {
			goPokedex = (GoPokedex) super.clone();
		} catch (CloneNotSupportedException e) {
			log.error("Clone failed.");
		}
		return goPokedex;
	}
}
