package jp.brainjuice.pokego.cache.inmemory;

import java.text.DecimalFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import jp.brainjuice.pokego.dao.jpa.CpMultiplierRepository;
import jp.brainjuice.pokego.dao.jpa.entity.CpMultiplier;
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

	/** indexで扱いたい場合に使用するリスト(ArrayList) */
	private static List<Map.Entry<String, Double>> cpMultiplierList;

	/**
	 * @throws PokemonDataInitException
	 */
	public CpMultiplierMap(CpMultiplierRepository cpMultiplierRepository) {
		init(cpMultiplierRepository);
	}

	/**
	 * @throws PokemonDataInitException
	 */
	public void init(CpMultiplierRepository cpMultiplierRepository) {

		try {
			List<CpMultiplier> cpMultList = cpMultiplierRepository.findAll();

			LinkedHashMap<String, Double> map = cpMultList.stream()
					.collect(Collectors.toMap(
							cm -> cm.getPl(),
							cm -> Double.valueOf(cm.getMultiplier()),
							(a, b) -> a,
							LinkedHashMap::new));
			this.putAll(map);

			// indexで扱いたい場合に使用するリスト
			cpMultiplierList = map.entrySet().stream()
					.collect(Collectors.toList());

			log.info("CpMultiplierMap generated!! (Referenced: table)");
		} catch (Exception e) {
			throw new PokemonDataInitException(e);
		}
	}

	/**
	 * 小数点以下を省略して呼び出された場合の考慮
	 */
	@Override
	public Double get(Object pl) {
		DecimalFormat df = new DecimalFormat("00.0");
		return super.get(df.format(Double.valueOf((String) pl)));
	}

	/**
	 * 小数点以下を省略して呼び出された場合の考慮
	 */
	@Override
	public boolean containsKey(Object pl) {
		DecimalFormat df = new DecimalFormat("00.0");
		return super.containsKey(df.format(Double.valueOf((String) pl)));
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

		DecimalFormat plFormat = new DecimalFormat("00.0");
		Map.Entry<String, Double> targetEntry = cpMultiplierList.stream()
				.filter(entry -> entry.getKey().equals(plFormat.format(Double.parseDouble(pl))))
				.findFirst().get();
		return cpMultiplierList.indexOf(targetEntry);
	}
}
