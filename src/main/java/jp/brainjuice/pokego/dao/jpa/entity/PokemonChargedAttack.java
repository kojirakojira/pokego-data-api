package jp.brainjuice.pokego.dao.jpa.entity;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import jp.brainjuice.pokego.business.constant.LearningPatternEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * ポケモンごとのスペシャル技
 *
 * @author saibabanagchampa
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "pokemon_charged_attack")
@ToString(exclude = {"chargedAttack", "attackAdditionalInfo"})
@IdClass(PokemonAttackPk.class)
public class PokemonChargedAttack implements Serializable, Cloneable {

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
	@Column(name = "learning_pattern", nullable = false, columnDefinition = "bpchar")
	private LearningPatternEnum learningPattern;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "move_id", insertable = false, updatable = false)
	private ChargedAttack chargedAttack;

	@OneToMany(fetch = FetchType.LAZY)
	@JoinColumns({
		@JoinColumn(name = "move_id", referencedColumnName = "move_id"),
		@JoinColumn(name = "pokedex_id", referencedColumnName = "pokedex_id")
	})
	private List<AttackAdditionalInfo> attackAdditionalInfo;

	/**
	 * (非 Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		int result = 17;
		result = result * 31 + pokedexId.hashCode();
		result = result * 31 + moveId.hashCode();
		return result;
	}

	/**
	 * (非 Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null || getClass() != obj.getClass()) return false;
		PokemonChargedAttack pca = (PokemonChargedAttack) obj;
		return Objects.equals(pokedexId, pca.getPokedexId()) && Objects.equals(moveId, pca.getMoveId());
	}

}
