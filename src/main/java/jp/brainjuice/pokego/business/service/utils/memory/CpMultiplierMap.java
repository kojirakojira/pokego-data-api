package jp.brainjuice.pokego.business.service.utils.memory;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import jp.brainjuice.pokego.utils.BjUtils;
import jp.brainjuice.pokego.utils.exception.PokemonDataInitException;
import lombok.extern.slf4j.Slf4j;


/**
 * CP Multiplier
 *
 * @author saibabanagchampa
 */
@Component
@Slf4j
public class CpMultiplierMap extends LinkedHashMap<String, Double> {

	private static final String FINE_NAME = "pokemon/cp-multiplier.yml";

	/** indexで扱いたい場合に使用するリスト(ArrayList) */
	private static List<Map.Entry<String, Double>> cpMultiplierList;

	/**
	 * 初期化処理（依存関係の都合でコンストラクタでは実行していない。）
	 *
	 * @throws PokemonDataInitException
	 */
	@PostConstruct
	public void init() throws PokemonDataInitException {

		try {
			CpMultiplierMap map = BjUtils.loadYaml(FINE_NAME, CpMultiplierMap.class);
			this.putAll(map);

			// indexで扱いたい場合に使用するリスト
			cpMultiplierList = map.entrySet().stream()
					.collect(Collectors.toList());

			log.info("CpMultiplierMap generated!!");
		} catch (Exception e) {
			throw new PokemonDataInitException(e);
		}
	}

	/**
	 * リストで取得する。
	 *
	 * @return
	 */
	public List<Map.Entry<String, Double>> getList() {
		return cpMultiplierList;
	}

	/**
	 * 最低CPを取得する。
	 *
	 * @return
	 */
	public String minPl() {
		return cpMultiplierList.get(0).getKey();
	}

	/**
	 * 最大PLを取得する。
	 *
	 * @return
	 */
	public String maxPl() {
		return cpMultiplierList.get(cpMultiplierList.size() - 1).getKey();
	}

	/**
	 * 指定されたPLのindexを取得する。
	 *
	 * @param pl
	 * @return
	 */
	public int indexOf(String pl) {

		Map.Entry<String, Double> targetEntry = cpMultiplierList.stream()
				.filter(entry -> entry.getKey().equals(pl))
				.findFirst().get();
		return cpMultiplierList.indexOf(targetEntry);
	}
}
