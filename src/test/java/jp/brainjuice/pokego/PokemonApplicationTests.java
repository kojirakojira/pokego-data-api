package jp.brainjuice.pokego;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import jp.brainjuice.pokego.business.dao.GoPokedexRepository;
import jp.brainjuice.pokego.business.dao.entity.GoPokedex;
import jp.brainjuice.pokego.business.service.utils.PokemonGoUtils;
import jp.brainjuice.pokego.business.service.utils.memory.CpMultiplierMap;
import jp.brainjuice.pokego.business.service.utils.memory.evo.EvolutionProvider;

@SpringBootTest
class PokemonApplicationTests {

	@Autowired
	GoPokedexRepository gpRepository;

	@Autowired
	PokemonGoUtils pokemonGoUtils;

	@Autowired
	CpMultiplierMap cpMultiplierMap;

	@Autowired
	EvolutionProvider evolutionProvider;

	@Test
	public void testCase() {
		List<String> lastList = evolutionProvider.getLastInEvoTree("0412N01");

		System.out.println(lastList);
	}

	/**
	 * 二分探索アルゴリズムのテスト
	 */
	@Test
	void contextLoads() {

		{
			int cp = binarySearchForPlIdxTest(0, 4, 0);
			System.out.println("expected: 1498, actual: " + cp);
		}

		{
			int cp = binarySearchForPlIdxTest(0, 5, 9);
			System.out.println("expected: 1482, actual: " + cp);
		}

		{
			int cp = binarySearchForPlIdxTest(0, 5, 1);
			System.out.println("expected: 1464, actual: " + cp);
		}

		{
			int cp = binarySearchForPlIdxTest(15, 15, 7);
			System.out.println("expected: 1457, actual: " + cp);
		}
	}

	private int binarySearchForPlIdxTest(int iva, int ivd, int ivh) {

		GoPokedex gp = gpRepository.findById("0143N01").get();

		Function<Double, Integer> calcCpFunc = (pl) -> pokemonGoUtils.calcCp(gp, iva, ivd, ivh, pl);

		Predicate<Integer> slCpLimitPredicate = (arg) -> { return arg.intValue() <= 1500; };

		int plIdx = pokemonGoUtils.binarySearchForPlIdx(
				slCpLimitPredicate,
				calcCpFunc);
		String pl = cpMultiplierMap.getList().get(plIdx).getKey();
		int cp = calcCpFunc.apply(cpMultiplierMap.get(pl));

		return cp;
	}

}
