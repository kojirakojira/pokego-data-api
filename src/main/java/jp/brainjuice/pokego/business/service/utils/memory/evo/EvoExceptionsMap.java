package jp.brainjuice.pokego.business.service.utils.memory.evo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jp.brainjuice.pokego.utils.BjUtils;
import jp.brainjuice.pokego.utils.exception.PokemonDataInitException;

@Component
class EvoExceptionsMap extends HashMap<String, List<String>> {

	private static final String EXCEPTIONS_FILE_NAME = "pokemon/pokemon-evolution-exceptions.yml";

	@Autowired
	EvoExceptionsMap() throws PokemonDataInitException {
		init();
	}

	private void init() throws PokemonDataInitException {
		// yamlファイルの内容をメモリに抱える。
		try {
			@SuppressWarnings("unchecked")
			Map<String, List<String>> yamlMap = BjUtils.loadYaml(EXCEPTIONS_FILE_NAME, HashMap.class);
			putAll(yamlMap);

		} catch (Exception e) {
			throw new PokemonDataInitException(e);
		}
	}
}
