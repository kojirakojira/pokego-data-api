package jp.brainjuice.pokego;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import jp.brainjuice.pokego.business.service.utils.memory.TypeChartInfo;

@SpringBootTest
class PokemonApplicationTests {


	@Autowired
	TypeChartInfo typeChartInfo;

	@Test
	void contextLoads() {
//
//		for (TypeEnum te1: TypeEnum.values()) {
//			System.out.println("------------------" + te1.name());
//			for (TypeEnum te2: TypeEnum.values()) {
//				if (te1 == te2) continue;
//				System.out.println(te2.name() + ": " + typeChartInfo.getDefenderTypes(te1, te2));
//			}
//		}
	}

}
