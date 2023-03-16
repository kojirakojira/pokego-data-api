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
	GoPokedexRepository goPokeRepository;

	@Autowired
	PokemonGoUtils pokemonGoUtils;

	@Test
	void contextLoads() {

		GoPokedex goPokedex = goPokeRepository.findById("0374N01").get();

		{
			int cp = pokemonGoUtils.calcCp(goPokedex, 15, 11, 6, "13");

			System.out.println(cp);
		}
		{
			int cp = pokemonGoUtils.calcCp(goPokedex, 8, 13, 10, "8");

			System.out.println(cp);
		}
	}

}
