package jp.brainjuice.pokego.business.service.search.utils.dto.moves;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class FastPvpParam {

	/** PvP時のダメージ */
	private double pvpPower;

	/** ゲージ増加量（PvP時） */
	private int energy;

	/** ターン数（1ターン=0.5秒） */
	private int turns;

	/** Damage Per Turns. round(pvpPower / turns, 2) */
	private double dpt;

	/** Energy Per Turns. round(energy / turns, 2) */
	private double ept;
}
