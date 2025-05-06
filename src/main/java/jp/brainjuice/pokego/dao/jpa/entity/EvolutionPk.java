package jp.brainjuice.pokego.dao.jpa.entity;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Evolutionの複合主キーを表現する。
 * pokedexIdのみを主キーにしたいところだが、ガーメイルのような進化前が複数存在する例外パターンを考慮して複合主キーにしている。
 * Pkのkが小文字なのは、Node.jsで痛い目を見たから。
 * @see Evolution
 */
@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class EvolutionPk implements Serializable {

	/** 図鑑ID */
	@Id
	@Column(name = "pokedex_id", nullable = false, columnDefinition = "bpchar")
	private String pokedexId;

	/** 進化前の図鑑ID */
	@Id
	@Column(name = "before_pokedex_id", nullable = false, columnDefinition = "bpchar")
	private String beforePokedexId;

	public EvolutionPk(Evolution evol) {
		setPokedexId(evol.getPokedexId());
		setBeforePokedexId(evol.getBeforePokedexId());
	}
}
