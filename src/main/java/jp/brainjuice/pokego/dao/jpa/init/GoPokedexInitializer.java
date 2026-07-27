package jp.brainjuice.pokego.dao.jpa.init;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import jp.brainjuice.pokego.dao.jpa.GoPokedexRepository;
import jp.brainjuice.pokego.utils.exception.PokemonDataInitException;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class GoPokedexInitializer implements BeanPostProcessor {

	/**
	 * go_pokedexをリフレッシュする。
	 *
	 * @throws PokemonDataInitException
	 */
	public void init(GoPokedexRepository goPokedexRepository) {

		try {
			goPokedexRepository.refresh();

		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new PokemonDataInitException(e);
		}

		log.info("go_pokedex materialized view initialized!!");
	}

	/**
	 * GoPokedexRepositoryが初期化された直後に実行する。
	 */
	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		if (bean instanceof GoPokedexRepository) {
			init((GoPokedexRepository) bean);
		}
		return bean;
	}
}
