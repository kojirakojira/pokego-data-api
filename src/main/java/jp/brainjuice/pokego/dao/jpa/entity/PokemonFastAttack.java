package jp.brainjuice.pokego.dao.jpa.entity;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import jp.brainjuice.pokego.business.service.search.utils.MovesUtils.MoveCategory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * ポケモンごとの通常技
 *
 * @author saibabanagchampa
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "pokemon_fast_attack")
@ToString
@IdClass(PokemonAttackPk.class)
public class PokemonFastAttack implements Serializable, Cloneable {

	/** 図鑑ID */
	@Id
	@Column(name = "move_id", nullable = false, columnDefinition = "bpchar")
	private String moveId;

	/** 技ID */
	@Id
	@Column(name = "pokedex_id", nullable = false, columnDefinition = "bpchar")
	private String pokedexId;

	/** 技のカテゴリ */
	@Enumerated(EnumType.STRING)
	@Column(name = "category", nullable = false, columnDefinition = "bpchar")
	private MoveCategory category;

}
