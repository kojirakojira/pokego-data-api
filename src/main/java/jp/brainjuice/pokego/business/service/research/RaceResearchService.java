package jp.brainjuice.pokego.business.service.research;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jp.brainjuice.pokego.business.dao.PokedexRepository;
import jp.brainjuice.pokego.business.dao.entity.GoPokedex;
import jp.brainjuice.pokego.business.dao.entity.Pokedex;
import jp.brainjuice.pokego.business.service.utils.dto.IndividialValue;
import jp.brainjuice.pokego.business.service.utils.memory.PokemonStatisticsInfo;
import jp.brainjuice.pokego.business.service.utils.memory.TooStrongPokemonList;
import jp.brainjuice.pokego.business.service.utils.memory.TypeMap;
import jp.brainjuice.pokego.web.form.res.elem.Race;
import jp.brainjuice.pokego.web.form.res.research.RaceResponse;

@Service
public class RaceResearchService implements ResearchService<RaceResponse> {

	private PokedexRepository pokedexRepository;

	private PokemonStatisticsInfo pokemonStatisticsInfo;

	private TooStrongPokemonList tooStrongPokemonList;

	private TypeMap typeMap;

	@Autowired
	public RaceResearchService(
			PokedexRepository pokedexRepository,
			PokemonStatisticsInfo pokemonStatisticsInfo,
			TooStrongPokemonList tooStrongPokemonList,
			TypeMap typeMap) {
		this.pokedexRepository = pokedexRepository;
		this.pokemonStatisticsInfo = pokemonStatisticsInfo;
		this.tooStrongPokemonList = tooStrongPokemonList;
		this.typeMap = typeMap;
	}

	@Override
	public void exec(IndividialValue iv, RaceResponse res) {

		String pokedexId = iv.getGoPokedex().getPokedexId();

		Pokedex pokedex = pokedexRepository.findById(pokedexId).get();
		GoPokedex goPokedex = iv.getGoPokedex();

		Race race = new Race(pokedex, goPokedex, typeMap);
		res.setRace(race);

		res.setTooStrong(tooStrongPokemonList.contains(pokedexId));

		res.setStatistics(pokemonStatisticsInfo.clone());
	}

}
