package jp.brainjuice.pokego.business.dao.dto;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jp.brainjuice.pokego.business.dao.EvolutionRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 図鑑IDとそれに対応する第一形態のポケモンの図鑑Noを保持する。
 *
 * @see EvolutionRepository#findAllBasePokedexNo()
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BasePokedexNo {

	@Id
	@Column(name = "target_pokedex_id")
	private String targetPokedexId;

	@Column(name = "pokedex_no")
	private Integer pokedexNo;

	public BasePokedexNo(Object[] objs) {
		setTargetPokedexId((String) objs[0]);
		setPokedexNo((Integer) objs[1]);
	}
}
