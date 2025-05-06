package jp.brainjuice.pokego.dao.jpa.init;

import org.springframework.stereotype.Component;

import jp.brainjuice.pokego.dao.jpa.TooStrongRepository;
import jp.brainjuice.pokego.utils.exception.PokemonDataInitException;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class TooStrongInitializer {

	public TooStrongInitializer(TooStrongRepository tooStrongRepository) throws PokemonDataInitException {

		init(tooStrongRepository);
	}

	/**
	 * go_pokedexをリフレッシュする。
	 *
	 * @throws PokemonDataInitException
	 */
	public void init(TooStrongRepository tooStrongRepository) throws PokemonDataInitException {

		try {
			tooStrongRepository.refresh();

		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new PokemonDataInitException(e);
		}

		log.info("too_strong materialized view initialized!!");
	}
}
