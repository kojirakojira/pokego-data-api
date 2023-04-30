package jp.brainjuice.pokego.business.service.utils.memory;

import java.io.InputStreamReader;
import java.util.LinkedHashMap;

import javax.annotation.PostConstruct;

import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

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

	@PostConstruct
	public void init() throws PokemonDataInitException {

		DefaultResourceLoader resourceLoader;
		InputStreamReader reader;
		try {
			resourceLoader = new DefaultResourceLoader();
			Resource resource = resourceLoader.getResource("classpath:pokemon/cp-multiplier.yml");
			reader = new InputStreamReader(resource.getInputStream());

			Yaml yaml = new Yaml();
			this.putAll(yaml.loadAs(reader, CpMultiplierMap.class));

			log.info("CpMultiplierMap generated!!");
		} catch (Exception e) {
			throw new PokemonDataInitException(e);
		}
	}
}
