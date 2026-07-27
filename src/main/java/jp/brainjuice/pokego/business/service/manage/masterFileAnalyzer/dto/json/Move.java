package jp.brainjuice.pokego.business.service.manage.masterFileAnalyzer.dto.json;

import lombok.Data;
import lombok.ToString;

/**
 * 通常技・スペシャル技兼用（ジム・レイド時）
 */
@Data
@ToString
public class Move {

	private String movementId;
	private String pokemonType;
	private double power;
	private int durationMs;
	private int damageWindowStartMs;
	private int damageWindowEndMs;
	private int energyDelta;
}
