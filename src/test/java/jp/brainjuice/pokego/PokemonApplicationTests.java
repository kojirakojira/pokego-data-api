package jp.brainjuice.pokego;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import jp.brainjuice.pokego.business.constant.Type.TypeEnum;
import jp.brainjuice.pokego.business.service.utils.memory.TypeChartInfo;

@SpringBootTest
class PokemonApplicationTests {


	@Autowired
	TypeChartInfo typeChartInfo;

	@Test
	void contextLoads() {

		for (TypeEnum te1: TypeEnum.values()) {
			System.out.println("------------------" + te1.name());
//			for (TypeEnum te2: TypeEnum.values()) {
////				if (te1 == te2) continue;
//				System.out.println(te2.name() + ": " + typeChartInfo.defenderScore(te1, te2));
//			}
			System.out.println(te1.name() + ": " + typeChartInfo.defenderScore(te1));
		}
//		System.out.println(typeChartInfo.getWeaknessTypes(TypeEnum.rock, TypeEnum.steel));
//		System.out.println(typeChartInfo.getWeaknessTypes(TypeEnum.steel, TypeEnum.rock));
//		System.out.println(typeChartInfo.getWeaknessTypes(TypeEnum.steel, null));
//		System.out.println(typeChartInfo.getWeaknessTypes(TypeEnum.steel, TypeEnum.steel));
	}

}
