package jp.brainjuice.pokego.business.service.search.utils.dto.damage;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class GymRaidAttackScoreOutDto {

	private double fastAttackScore;
	private double chargedAttackScore;
	private double attackScore;
}
