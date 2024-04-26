package jp.brainjuice.pokego.business.service.utils.memory.evo;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import com.amazonaws.services.s3.model.S3Object;

import jp.brainjuice.pokego.utils.BjUtils;
import jp.brainjuice.pokego.utils.exception.PokemonDataInitException;
import jp.brainjuice.pokego.utils.external.AwsS3Utils;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
class EvoExceptionsMap extends HashMap<String, List<String>> {

	private AwsS3Utils awsS3Utils;

	/** pokemon-evolution-exceptions.yml */
	private static final String S3_POKEMON_EVO_EXCEPTIONS_CSV_FILE_NAME = "pokego/data/evolution/pokemon-evolution-exceptions{0}.yml";

	@Autowired
	EvoExceptionsMap(AwsS3Utils awsS3Utils) throws PokemonDataInitException {
		this.awsS3Utils = awsS3Utils;
		init();
	}

	private void init() throws PokemonDataInitException {

		String fileName = "";
		try {
			fileName = MessageFormat.format(S3_POKEMON_EVO_EXCEPTIONS_CSV_FILE_NAME, awsS3Utils.getSuffix());
			S3Object object = awsS3Utils.download(fileName);
			Resource resource = new InputStreamResource(object.getObjectContent());
			@SuppressWarnings("unchecked")
			Map<String, List<String>> yamlMap = BjUtils.convYaml(resource, HashMap.class);

			// yamlファイルの内容をメモリに抱える。
			putAll(yamlMap);

		} catch (Exception e) {
			throw new PokemonDataInitException(e);
		}

		log.info(MessageFormat.format("EvolutionExceptionsMap generated!! (Referenced file: {0}{1})", awsS3Utils.getEndpoint(), fileName));
	}
}
