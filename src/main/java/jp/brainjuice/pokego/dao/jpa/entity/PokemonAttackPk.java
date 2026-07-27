package jp.brainjuice.pokego.dao.jpa.entity;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Id;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * ポケモンごとの技の主キー
 * 通常技、スペシャル技で併用している。
 */
@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class PokemonAttackPk implements Serializable {

	/** 図鑑ID */
	@Id
	@Column(name = "pokedex_id", nullable = false, columnDefinition = "bpchar")
	private String pokedexId;

	/** 技ID */
	@Id
	@Column(name = "move_id", nullable = false, columnDefinition = "bpchar")
	private String moveId;

	public PokemonAttackPk(PokemonChargedAttack pca) {
		setPokedexId(pca.getPokedexId());
		setMoveId(pca.getMoveId());
	}
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
		PokemonAttackPk paPk = (PokemonAttackPk) obj;
		return Objects.equals(pokedexId, paPk.getPokedexId()) && Objects.equals(moveId, paPk.getMoveId());
	}
}
