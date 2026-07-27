package jp.brainjuice.pokego.business.service.search.utils.dto.damage;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class GymRaidTenMinAttackCounts {

	// 処理効率化の都合上doubleで保持する。
	private double fastAttackCount;
	private double chargedAttackCount;
}
