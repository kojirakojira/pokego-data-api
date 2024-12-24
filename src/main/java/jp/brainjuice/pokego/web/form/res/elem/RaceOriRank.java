package jp.brainjuice.pokego.web.form.res.elem;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * そのポケモンの原作種族値における順位を保持する。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RaceOriRank {

	private int hp;
	private int attack;
	private int defense;
	private int specialAttack;
	private int specialDefense;
	private int speed;
}
