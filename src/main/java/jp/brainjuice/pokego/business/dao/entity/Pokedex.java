package jp.brainjuice.pokego.business.dao.entity;

import javax.annotation.Nonnull;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name="pokedex")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Pokedex {

	/** 図鑑No(4) + 亜種フラグ(1) + 連番(2) */
	@Id
	@Column(name="pokedex_id")
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
}
