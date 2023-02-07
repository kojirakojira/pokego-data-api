package jp.brainjuice.pokego;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import jp.brainjuice.pokego.business.dao.GoPokedexRepository;
import jp.brainjuice.pokego.business.dao.PokedexFilterInfoRepository;
import jp.brainjuice.pokego.business.dao.PokedexFilterInfoRepository.FilterEnum;

@SpringBootTest
class PokemonApplicationTests {

	@Autowired
	PokedexFilterInfoRepository pfiRepository;

	@Autowired
	GoPokedexRepository goPokeRepository;

	@Test
	void contextLoads() {
		Map<FilterEnum, Object> filterMap = new HashMap<>();
		filterMap.put(FilterEnum.impled, false);
		filterMap.put(FilterEnum.mega, true);
		List<String> pidList = pfiRepository.findByAny(filterMap);

		goPokeRepository.findAllById(pidList).forEach(System.out::println);
	}

}
