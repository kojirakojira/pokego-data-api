package jp.brainjuice.pokego.dao.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "too_strong")
public class TooStrong {

	@Id
	@Column(name = "pokedex_id", nullable = false, columnDefinition = "bpchar")
	private String pokedexId;
}
