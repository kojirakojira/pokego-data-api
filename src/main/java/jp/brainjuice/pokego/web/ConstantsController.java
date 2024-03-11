package jp.brainjuice.pokego.web;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jp.brainjuice.pokego.business.constant.GenNameEnum;
import jp.brainjuice.pokego.business.constant.RegionEnum;
import jp.brainjuice.pokego.business.constant.SituationEnum;
import jp.brainjuice.pokego.business.constant.Type.TypeColorEnum;
import jp.brainjuice.pokego.business.constant.Type.TypeEnum;
import jp.brainjuice.pokego.business.dao.PokedexFilterInfoRepository.FilterEnum;
import jp.brainjuice.pokego.business.service.utils.memory.CpMultiplierMap;
import jp.brainjuice.pokego.web.form.res.elem.Color;
import jp.brainjuice.pokego.web.form.res.elem.TypeInfo;

/**
 * 定数情報を取得するコントローラクラス
 *
 * @author saibabanagchampa
 *
 */
@RestController
@RequestMapping("/api")
public class ConstantsController {

	private CpMultiplierMap cpMultiplierMap;

	public ConstantsController(CpMultiplierMap cpMultiplierMap) {
		this.cpMultiplierMap = cpMultiplierMap;
	}

	@GetMapping("/typeConst")
	public List<TypeInfo> typeConst() {
		List<TypeInfo> typeInfoList = Stream.of(TypeEnum.values())
				.map(te -> {
					TypeColorEnum color = TypeColorEnum.valueOf(te.name());
					return new TypeInfo(te, te.getJpn(), new Color(color.getR(), color.getG(), color.getB()));
				})
				.collect(Collectors.toList());
		return typeInfoList;
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
		Map<String, String> genMap = new LinkedHashMap<>();
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

	@GetMapping("/situationConst")
	public Map<String, String> situationConst() {
		Map<String, String> situationMap = new LinkedHashMap<>();
		for (SituationEnum item: SituationEnum.values()) {
			situationMap.put(item.name(), item.getJpn());
		}
		return situationMap;
	}
}
