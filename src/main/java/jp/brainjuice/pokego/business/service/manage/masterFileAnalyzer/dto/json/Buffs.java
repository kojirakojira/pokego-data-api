package jp.brainjuice.pokego.business.service.manage.masterFileAnalyzer.dto.json;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class Buffs {

	private int attackerAttackStatStageChange;
	private int attackerDefenseStatStageChange;
	private int targetAttackStatStageChange;
	private int targetDefenseStatStageChange;
	private double buffActivationChance;
}
