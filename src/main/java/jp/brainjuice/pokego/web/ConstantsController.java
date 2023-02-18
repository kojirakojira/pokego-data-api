package jp.brainjuice.pokego.web;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jp.brainjuice.pokego.business.constant.GenNameEnum;
import jp.brainjuice.pokego.business.constant.RegionEnum;
import jp.brainjuice.pokego.business.constant.Type.TypeEnum;

@RestController
@RequestMapping("/api")
public class ConstantsController {

	@GetMapping("/typeConst")
	public Map<String, String> typeConst() {
		Map<String, String> typeMap = new HashMap<>();
		for (TypeEnum type: TypeEnum.values()) {
			typeMap.put(type.name(), type.getJpn());
		}
		return typeMap;
	}

	@GetMapping("/regionConst")
	public Map<String, String> regionConst() {
		Map<String, String> regionMap = new HashMap<>();
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
}
