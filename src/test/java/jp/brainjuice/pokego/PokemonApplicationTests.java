package jp.brainjuice.pokego;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import jp.brainjuice.pokego.business.service.XTypeService;
import jp.brainjuice.pokego.web.form.res.XTypeResponse;

@SpringBootTest
class PokemonApplicationTests {


	@Autowired
	XTypeService xTypeService;
//	TypeChartInfo typeChartInfo;

	@Test
	void contextLoads() {

		XTypeResponse res = new XTypeResponse();
		xTypeService.exec("x", "bug", "fire", "flying", "none", res);

//		res.getTypeRankList().forEach(elem -> System.out.println(elem.getRank() + "位 " + elem.getTwoTypeKey() + ": " + elem.getMessages()));

//		System.out.println(typeChartInfo.getEffective(TypeEnum.electric, TypeEnum.water).get());
//		System.out.println(typeChartInfo.getEffective(TypeEnum.electric, new TwoTypeKey(TypeEnum.water, null)).get());

//		final TypeEnum[] arr = TypeEnum.values();
//		final TypeEnum[] values = new TypeEnum[arr.length + 1];
//		System.arraycopy(arr, 0, values, 0, arr.length);
//
//		for (TypeEnum te1: values) {
//			for (TypeEnum te2: values) {
//				for (TypeEnum te3: values) {
//					if (te1 == te2) continue;
//					xTypeService.exec(
//							(te1 == null ? null: te1.name()),
//							(te2 == null ? null: te2.name()),
//							"x",
//							(te3 == null ? null: te3.name()),
//							"defense");
//				}
//			}
//		}
	}

}
