package jp.brainjuice.pokego.business.dao.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "race_exceptions")
public class RaceExceptions {

	/** 図鑑No(4) + 亜種コード(1) + 連番(2) */
	@Id
	@Column(name = "pokedex_id", nullable = false, columnDefinition = "bpchar")
	private String pokedexId;

	/** HP */
	private Integer hp;

	/** こうげき */
	private Integer attack;

	/** ぼうぎょ */
	private Integer defense;

	@Column(name = "not_exists_origin")
	private Boolean notExistsOrigin;
}
