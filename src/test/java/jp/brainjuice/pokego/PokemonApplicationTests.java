package jp.brainjuice.pokego;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import jp.brainjuice.pokego.business.dao.GoPokedexRepository;
import jp.brainjuice.pokego.business.dao.entity.GoPokedex;
import jp.brainjuice.pokego.business.service.utils.PokemonGoUtils;

@SpringBootTest
class PokemonApplicationTests {

	@Autowired
	GoPokedexRepository gpRepository;

	@Autowired
	PokemonGoUtils pokemonGoUtils;

	@Test
	void contextLoads() {

		GoPokedex gp = gpRepository.findById("0129N01").get();

		String pl = pokemonGoUtils.calcPl(gp, 0, 0, 0, 100);

		System.out.println(pl);
	}

}
