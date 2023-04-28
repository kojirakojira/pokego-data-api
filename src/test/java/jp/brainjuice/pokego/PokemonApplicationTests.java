package jp.brainjuice.pokego;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import jp.brainjuice.pokego.business.service.IroiroTypeRankService;
import jp.brainjuice.pokego.business.service.IroiroTypeRankService.IroiroTypeRankSearchPattern;
import jp.brainjuice.pokego.web.form.res.IroiroTypeRankResponse;

@SpringBootTest
class PokemonApplicationTests {


	@Autowired
	IroiroTypeRankService iroiroTypeRankService;

	@Test
	void contextLoads() {
		IroiroTypeRankResponse res = new IroiroTypeRankResponse();
		iroiroTypeRankService.exec(IroiroTypeRankSearchPattern.leastWeakness, res);

		res.getTypeRankList().forEach(System.out::println);
	}

}
