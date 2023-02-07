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
import lombok.Getter;
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

	public enum TypeEnum {
		normal("ノーマル"),
		fire("ほのお"),
		water("みず"),
		grass("くさ"),
		electric("でんき"),
		ice("こおり"),
		fighting("かくとう"),
		poison("どく"),
		ground("じめん"),
		flying("ひこう"),
		psychic("エスパー"),
		bug("むし"),
		rock("いわ"),
		ghost("ゴースト"),
		dragon("ドラゴン"),
		dark("あく"),
		steel("はがね"),
		fairy("フェアリー");

		@Getter
		private final String name;

		TypeEnum(String name) {
			this.name = name;
		}


		public static TypeEnum getEnumName(String str) {
			for(TypeEnum v : values()) {
				if(v.getName().equals(str)) {
					return v;
				}
			}
			return null;
		}
	}

	/**
	 * マップのキーとなる値。
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
