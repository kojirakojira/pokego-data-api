package jp.brainjuice.pokego;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.assertj.core.util.Arrays;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import jp.brainjuice.pokego.business.constant.Type.TypeEnum;
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
//		filterMap.put(FilterEnum.impled, false);
//		filterMap.put(FilterEnum.mega, true);
		filterMap.put(FilterEnum.twoType, Arrays.asList(Arrays.array(TypeEnum.psychic, TypeEnum.fairy)));
		List<String> pidList = pfiRepository.findByAny(filterMap);
//		List<String> pidList = pfiRepository.findIdByType(TypeEnum.psychic, TypeEnum.fairy);

		goPokeRepository.findAllById(pidList).forEach(System.out::println);
	}

}
