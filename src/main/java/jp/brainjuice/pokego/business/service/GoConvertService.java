package jp.brainjuice.pokego.business.service;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import jp.brainjuice.pokego.business.dao.GoPokedexRepository;
import jp.brainjuice.pokego.business.dao.PokedexRepository;
import jp.brainjuice.pokego.business.dao.entity.GoPokedex;
import jp.brainjuice.pokego.business.dao.entity.Pokedex;
import jp.brainjuice.pokego.business.service.utils.PokemonUtils;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class GoConvertService {

	private PokedexRepository pokedexRepository;

	private GoPokedexRepository goPokedexRepository;

	private PokemonUtils pokemonUtils;

	@Autowired
	public GoConvertService(
			PokedexRepository pokedexRepository,
			GoPokedexRepository goPokedexRepository,
			PokemonUtils pokemonUtils) {
		this.pokedexRepository = pokedexRepository;
		this.goPokedexRepository = goPokedexRepository;
		this.pokemonUtils = pokemonUtils;
	}

	@EventListener(ApplicationReadyEvent.class)
	public void init() {
		insertGoPokedexAll();
		log.info("GoPokedex table generated!!");
	}

	/**
	 * pokedexテーブルを元にgo_pokedexテーブルを生成します。
	 *
	 * @return
	 */
	public String insertGoPokedexAll() {


		List<Pokedex> pokeList = pokedexRepository.findAll();

		List<GoPokedex> goPokeList = new ArrayList<GoPokedex>();
		pokeList.forEach(poke -> {
			goPokeList.add(pokemonUtils.getGoPokedex(poke));
		});

		goPokedexRepository.saveAll(goPokeList);

		return "成功！！";
	}

	public String getGoStatus(String pokedexId) {

		Pokedex pokedex = pokedexRepository.findById(pokedexId).get();

		GoPokedex goPokedex = pokemonUtils.getGoPokedex(pokedex);

		return MessageFormat.format(
				"No:{0}, Pokemon:{1}, AT:{2}, DF:{3}, HP:{4}",
				goPokedex.getPokedexId(),
				goPokedex.getName(),
				goPokedex.getAttack(),
				goPokedex.getDefense(),
				goPokedex.getHp());
	}
}
