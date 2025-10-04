package jp.brainjuice.pokego.dao.jpa.dto;

import jp.brainjuice.pokego.dao.jpa.FastAttackRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * @see FastAttackRepository#findSimpMoveByNameLikeIn()
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SimpMove {

	private String moveId;
	private String name;

	public SimpMove(Object[] objs) {
		setMoveId((String) objs[0]);
		setName((String) objs[1]);
	}
}
