package jp.brainjuice.pokego.business.dao.init;

import org.springframework.stereotype.Component;

import jp.brainjuice.pokego.business.dao.GoPokedexRepository;
import jp.brainjuice.pokego.utils.exception.PokemonDataInitException;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class GoPokedexInitializer {

	public GoPokedexInitializer(GoPokedexRepository goPokedexRepository) throws PokemonDataInitException {

		init(goPokedexRepository);
	}

	/**
	 * go_pokedexをリフレッシュする。
	 *
	 * @throws PokemonDataInitException
	 */
	public void init(GoPokedexRepository goPokedexRepository) throws PokemonDataInitException {

		try {
			goPokedexRepository.refresh();

		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new PokemonDataInitException(e);
		}

		log.info("go_pokedex materialized view initialized!!");
	}
}
