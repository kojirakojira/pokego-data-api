package jp.brainjuice.pokego.business.service.search.utils.dto.damage;

import jp.brainjuice.pokego.business.constant.WeatherBoosts.WeatherEnum;
import jp.brainjuice.pokego.business.service.search.utils.dto.type.TwoTypeKey;
import jp.brainjuice.pokego.dao.jpa.entity.ChargedAttack;
import jp.brainjuice.pokego.dao.jpa.entity.FastAttack;
import jp.brainjuice.pokego.dao.jpa.entity.GoPokedex;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GymRaidAttackScoreInDto {

	private GoPokedex goPokedex;
	private FastAttack fastAttack;
	private ChargedAttack chargedAttack;
	private TwoTypeKey defenderType;
	private WeatherEnum weather;
	private boolean isShadow;
}
