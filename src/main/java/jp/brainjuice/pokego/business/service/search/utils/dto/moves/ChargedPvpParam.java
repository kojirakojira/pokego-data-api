package jp.brainjuice.pokego.business.service.search.utils.dto.moves;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ChargedPvpParam {

	/** PvP時のダメージ */
	private double pvpPower;

	/** ゲージ増加量（PvP時） */
	private int energy;

	/** Power Per Energy. round(pvpPower / abs(energyIncrAmount), 2) */
	private double dpe;

	/** 技のバフ・デバフ効果 */
	private Buff buff;
}
