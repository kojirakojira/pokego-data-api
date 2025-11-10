package jp.brainjuice.pokego.business.service.search.utils;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import jp.brainjuice.pokego.business.constant.Type.TypeEnum;
import jp.brainjuice.pokego.business.service.search.utils.dto.damage.GymRaidAttackScoreInDto;
import jp.brainjuice.pokego.business.service.search.utils.dto.damage.GymRaidAttackScoreOutDto;
import jp.brainjuice.pokego.business.service.search.utils.dto.damage.GymRaidTenMinAttackCounts;
import jp.brainjuice.pokego.business.service.search.utils.dto.type.TwoTypeKey;
import jp.brainjuice.pokego.dao.jpa.entity.ChargedAttack;
import jp.brainjuice.pokego.dao.jpa.entity.FastAttack;
import jp.brainjuice.pokego.dao.jpa.entity.GoPokedex;

@SpringBootTest
public class GymRaidDamageCalculatorTest {

	@Autowired
	private GymRaidDamageCalculator calculator;

	@Test
	public void attackScore_ペリッパー() throws Exception {

		FastAttack fa = new FastAttack();
		fa.setName("みずでっぽう");
		fa.setType(TypeEnum.water);
		fa.setGymPower(5);
		fa.setGymEnergyIncrAmount(5);
		fa.setDamageMs(300);
		fa.setTotalMs(500);
		ChargedAttack ca = new ChargedAttack();
		ca.setName("ハイドロポンプ");
		ca.setType(TypeEnum.water);
		ca.setGymPower(135);
		ca.setGymEnergyIncrAmount(-100);
		ca.setTotalMs(3500);
		GoPokedex gp = new GoPokedex();
		gp.setAttack(150);
		gp.setType1(TypeEnum.water);
		gp.setType2(TypeEnum.flying);
		TwoTypeKey defender = new TwoTypeKey(TypeEnum.normal, null);
		GymRaidAttackScoreInDto in = new GymRaidAttackScoreInDto(gp, fa, ca, defender, null, false);
		GymRaidAttackScoreOutDto out = calculator.attackScore(in);

		System.out.println(out);
	}

	@Test
	public void attackScore_シャドウコイキング() {

		FastAttack fa = new FastAttack();
		fa.setName("はねる");
		fa.setType(TypeEnum.water);
		fa.setGymPower(0);
		fa.setGymEnergyIncrAmount(17);
		fa.setDamageMs(800);
		fa.setTotalMs(1500);
		ChargedAttack ca = new ChargedAttack();
		ca.setName("やつあたり");
		ca.setType(TypeEnum.normal);
		ca.setGymPower(10);
		ca.setGymEnergyIncrAmount(-33);
		ca.setTotalMs(2000);
		GoPokedex gp = new GoPokedex();
		gp.setAttack(150);
		gp.setType1(TypeEnum.water);
		gp.setType2(TypeEnum.flying);
		TwoTypeKey defender = new TwoTypeKey(TypeEnum.normal, null);
		GymRaidAttackScoreInDto in = new GymRaidAttackScoreInDto(gp, fa, ca, defender, null, true);
		GymRaidAttackScoreOutDto out = calculator.attackScore(in);

		System.out.println(out);
	}

	@Test
	public void testCase002() throws Exception {

		GymRaidDamageCalculator calculator = new GymRaidDamageCalculator(null, null);

		FastAttack fa = new FastAttack();
		fa.setName("みずでっぽう");
		fa.setGymEnergyIncrAmount(5);
		fa.setDamageMs(300);
		fa.setTotalMs(500);
		ChargedAttack ca = new ChargedAttack();
		ca.setName("ハイドロポンプ");
		ca.setGymEnergyIncrAmount(-100);
		ca.setTotalMs(3500);
		GymRaidTenMinAttackCounts counts = calculator.calcTenMinAttackCounts(fa, ca);

		assertEquals(892.0D, counts.getFastAttackCount());
		assertEquals(44.0D, counts.getChargedAttackCount());
	}

	@Test
	public void testCase003() throws Exception {

		GymRaidDamageCalculator calculator = new GymRaidDamageCalculator(null, null);

		FastAttack fa = new FastAttack();
		fa.setName("やきつくす");
		fa.setGymEnergyIncrAmount(22);
		fa.setDamageMs(700);
		fa.setTotalMs(2500);
		ChargedAttack ca = new ChargedAttack();
		ca.setName("ニトロチャージ");
		ca.setGymEnergyIncrAmount(-33);
		ca.setTotalMs(4000);
		GymRaidTenMinAttackCounts counts = calculator.calcTenMinAttackCounts(fa, ca);

		assertEquals(117.0D, counts.getFastAttackCount());
		assertEquals(77.0D, counts.getChargedAttackCount());
	}
}
