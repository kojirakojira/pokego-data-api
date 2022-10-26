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

/**
 * 世代情報とその名称を管理する。
 *
 * @author saibabanagchampa
 *
 */
@Component
@Slf4j
public class GenNameMap extends HashMap<String, String> {

	/**
	 * gen-name.ymlを読み取る。
	 *
	 * @throws PokemonDataInitException
	 */
	@SuppressWarnings("unchecked")
	@PostConstruct
	public void init() {

		DefaultResourceLoader resourceLoader;
		InputStreamReader reader;
		try {
			resourceLoader = new DefaultResourceLoader();
			Resource resource = resourceLoader.getResource("classpath:config/gen-name.yml");
			reader = new InputStreamReader(resource.getInputStream());

			Yaml yaml = new Yaml();
			this.putAll(yaml.loadAs(reader, Map.class));

		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}
}
