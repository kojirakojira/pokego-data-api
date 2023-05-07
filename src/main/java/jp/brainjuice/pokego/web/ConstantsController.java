package jp.brainjuice.pokego.web;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jp.brainjuice.pokego.business.constant.GenNameEnum;
import jp.brainjuice.pokego.business.constant.RegionEnum;
import jp.brainjuice.pokego.business.constant.Type.TypeColorEnum;
import jp.brainjuice.pokego.business.constant.Type.TypeEnum;
import jp.brainjuice.pokego.business.dao.PokedexFilterInfoRepository.FilterEnum;
import jp.brainjuice.pokego.business.service.utils.memory.CpMultiplierMap;

@RestController
@RequestMapping("/api")
public class ConstantsController {

	private CpMultiplierMap cpMultiplierMap;

	public ConstantsController(CpMultiplierMap cpMultiplierMap) {
		this.cpMultiplierMap = cpMultiplierMap;
	}

	@GetMapping("/typeConst")
	public Map<String, Map<String, Object>> typeConst() {
		final Map<String, Map<String, Object>> typeMap = new LinkedHashMap<>();
		for (TypeEnum type: TypeEnum.values()) {

			final Map<String, Object> map = new HashMap<>();

			map.put("jpn", type.getJpn());

			TypeColorEnum color = TypeColorEnum.valueOf(type.name());
			map.put("r", color.getR());
			map.put("g", color.getG());
			map.put("b", color.getB());
			typeMap.put(type.name(), map);
		}
		return typeMap;
	}

	@GetMapping("/regionConst")
	public Map<String, String> regionConst() {
		Map<String, String> regionMap = new LinkedHashMap<>();
		for (RegionEnum region: RegionEnum.values()) {
			if (region == RegionEnum.none) continue;
			regionMap.put(region.name(), region.getJpn());
		}
		return regionMap;
	}

	@GetMapping("/genConst")
	public Map<String, String> genConst() {
		Map<String, String> genMap = new TreeMap<>();
		for (GenNameEnum gen: GenNameEnum.values()) {
			genMap.put(gen.name(), gen.getJpn());
		}
		return genMap;
	}

	@GetMapping("/filterItemsConst")
	public Map<String, String> filterItemsConst() {
		Map<String, String> filterMap = new HashMap<>();
		for (FilterEnum item: FilterEnum.values()) {
			filterMap.put(item.name(), item.getJpn());
		}
		return filterMap;
	}

	@GetMapping("/plConst")
	public List<String> genPl() {
		return cpMultiplierMap.entrySet().stream()
				.map(Map.Entry::getKey)
				.collect(Collectors.toList());
	}
}
