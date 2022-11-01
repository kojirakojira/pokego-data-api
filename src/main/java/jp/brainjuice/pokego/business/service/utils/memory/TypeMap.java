package jp.brainjuice.pokego.business.service.utils.memory;

import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import jp.brainjuice.pokego.utils.exception.PokemonDataInitException;
import lombok.extern.slf4j.Slf4j;


@Component
@Slf4j
public class TypeMap extends HashMap<String, Map<String, Integer>> {

	@PostConstruct
	public void init() throws PokemonDataInitException {

		DefaultResourceLoader resourceLoader;
		InputStreamReader reader;
		try {
			resourceLoader = new DefaultResourceLoader();
			Resource resource = resourceLoader.getResource("classpath:config/type-map.yml");
			reader = new InputStreamReader(resource.getInputStream());

			Yaml yaml = new Yaml();
			this.putAll(yaml.loadAs(reader, TypeMap.class));

			log.info("TypeMap generated!!");
		} catch (Exception e) {
			throw new PokemonDataInitException(e);
		}
	}

	/**
	 * マップのキーとなる値。（タイプはここでは定義しない。）
	 *
	 * @author saibabanagchampa
	 *
	 */
	public enum KeyElem {

		/** 赤色 */
		r,
		/** 緑色 */
		g,
		/** 青色 */
		b,
	}
}
