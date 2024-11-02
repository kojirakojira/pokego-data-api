package jp.brainjuice.pokego.cache.dao.jpa.entity;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Table(name = "pokemon_view")
public class PokemonView {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pokemon_view_seq")
	@SequenceGenerator(name = "pokemon_view_seq", sequenceName = "pokemon_view_seq", initialValue = 1, allocationSize = 1)
	@Column(name = "pokemon_view_id")
	private Integer pokemonViewId;

	@Column(name = "pokedex_id", nullable = false, columnDefinition = "bpchar")
	private String pokedexId;

	@Column(nullable = false)
	private Date ymd;

	@Column(name = "view_count", nullable = false)
	private int viewCount;

}
