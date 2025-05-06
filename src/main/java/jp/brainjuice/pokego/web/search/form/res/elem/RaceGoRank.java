package jp.brainjuice.pokego.web.search.form.res.elem;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * そのポケモンのGO種族値における順位を保持する。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RaceGoRank {

	private int hp;
	private int attack;
	private int defense;
}
