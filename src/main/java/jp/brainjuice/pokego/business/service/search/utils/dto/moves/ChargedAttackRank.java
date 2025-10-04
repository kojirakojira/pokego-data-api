package jp.brainjuice.pokego.business.service.search.utils.dto.moves;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class ChargedAttackRank {
	/*
	 * ジム・レイド
	 */
	/** ダメージ */
	private int gymPowerRank;
	/** ゲージ増加量 */
	private int gymEnergyIncrAmountRank;
	/** DPS */
	private int dpsRank;
	/** 技の発生時間 */
	private int damageSecondsRank;

	/*
	 * PvP
	 */
	/** ダメージ */
	private int pvpPowerRank;
	/** ゲージ増加量 */
	private int pvpEnergyIncrAmountRank;
	/** DPE */
	private int dpeRank;

	/** 技の総数 */
	private int totalCount;
}
