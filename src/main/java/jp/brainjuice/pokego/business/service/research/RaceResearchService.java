package jp.brainjuice.pokego.business.service.research;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jp.brainjuice.pokego.business.dao.GoPokedexRepository;
import jp.brainjuice.pokego.business.dao.PokedexRepository;
import jp.brainjuice.pokego.business.dao.entity.GoPokedex;
import jp.brainjuice.pokego.business.dao.entity.Pokedex;
import jp.brainjuice.pokego.business.service.utils.dto.IndividialValue;
import jp.brainjuice.pokego.web.form.res.research.RaceResponse;

@Service
public class RaceResearchService implements ResearchService<RaceResponse> {

	private PokedexRepository pokedexRepository;

	private GoPokedexRepository goPokedexRepository;

	@Autowired
	public RaceResearchService(
			PokedexRepository pokedexRepository,
			GoPokedexRepository goPokedexRepository) {
		this.pokedexRepository = pokedexRepository;
		this.goPokedexRepository = goPokedexRepository;
	}

	@Override
	public void exec(IndividialValue iv, RaceResponse res) {

		String pokedexId = iv.getGoPokedex().getPokedexId();

		Pokedex pokedex = pokedexRepository.findById(pokedexId).get();
		GoPokedex goPokedex = goPokedexRepository.findById(pokedexId).get();

		res.setPokedex(pokedex);
		res.setGoPokedex(goPokedex);

	}

}
