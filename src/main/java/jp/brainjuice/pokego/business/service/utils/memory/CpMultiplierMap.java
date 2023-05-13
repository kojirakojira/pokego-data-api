package jp.brainjuice.pokego.business.service.utils.memory;

import java.util.LinkedHashMap;

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

	@PostConstruct
	public void init() throws PokemonDataInitException {

		try {
			CpMultiplierMap map = BjUtils.loadYaml(FINE_NAME, CpMultiplierMap.class);
			this.putAll(map);

			log.info("CpMultiplierMap generated!!");
		} catch (Exception e) {
			throw new PokemonDataInitException(e);
		}
	}
}
