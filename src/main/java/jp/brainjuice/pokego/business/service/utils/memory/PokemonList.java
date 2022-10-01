package jp.brainjuice.pokego.business.service.utils.memory;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jp.brainjuice.pokego.business.dao.PokedexRepository;
import jp.brainjuice.pokego.business.dao.entity.Pokedex;
import jp.brainjuice.pokego.filter.log.LogUtils;


@Component
public class PokemonList extends ArrayList<String> {

	private PokedexRepository pokedexRepository;

	@Autowired
	public PokemonList(PokedexRepository pokedexRepository) {
		this.pokedexRepository = pokedexRepository;
	}

	@PostConstruct
	public void init() {

		try {
			List<Pokedex> pokedexList = pokedexRepository.findAll();
			pokedexList.forEach(p -> add(p.getName()));

			LogUtils.getLog(this).debug("PokemonList: " + this.toString());
			LogUtils.getLog(this).info("PokemonList generated!!");
		} catch (Exception e) {
			LogUtils.getLog(this).error(e.getMessage(), e);
		}
	}
}
