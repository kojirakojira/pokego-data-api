package jp.brainjuice.pokego.business.service.research;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jp.brainjuice.pokego.business.dao.GoPokedexRepository;
import jp.brainjuice.pokego.business.dao.PokedexRepository;
import jp.brainjuice.pokego.business.dao.entity.GoPokedex;
import jp.brainjuice.pokego.business.dao.entity.Pokedex;
import jp.brainjuice.pokego.business.service.utils.dto.IndividialValue;
import jp.brainjuice.pokego.business.service.utils.dto.IndividialValue.ParamsEnum;
import jp.brainjuice.pokego.business.service.utils.memory.EvolutionInfo;
import jp.brainjuice.pokego.business.service.utils.memory.PokemonStatisticsInfo;
import jp.brainjuice.pokego.business.service.utils.memory.TooStrongPokemonList;
import jp.brainjuice.pokego.business.service.utils.memory.TypeMap;
import jp.brainjuice.pokego.web.form.res.MsgLevelEnum;
import jp.brainjuice.pokego.web.form.res.elem.Race;
import jp.brainjuice.pokego.web.form.res.research.RaceResponse;

@Service
public class RaceResearchService implements ResearchService<RaceResponse> {

	private PokedexRepository pokedexRepository;

	private GoPokedexRepository goPokedexRepository;

	private PokemonStatisticsInfo pokemonStatisticsInfo;

	private TooStrongPokemonList tooStrongPokemonList;

	private EvolutionInfo evolutionInfo;

	private TypeMap typeMap;

	@Autowired
	public RaceResearchService(
			PokedexRepository pokedexRepository,
			GoPokedexRepository goPokedexRepository,
			PokemonStatisticsInfo pokemonStatisticsInfo,
			TooStrongPokemonList tooStrongPokemonList,
			EvolutionInfo evolutionInfo,
			TypeMap typeMap) {
		this.pokedexRepository = pokedexRepository;
		this.goPokedexRepository = goPokedexRepository;
		this.pokemonStatisticsInfo = pokemonStatisticsInfo;
		this.tooStrongPokemonList = tooStrongPokemonList;
		this.evolutionInfo = evolutionInfo;
		this.typeMap = typeMap;
	}

	@Override
	public void exec(IndividialValue iv, RaceResponse res) {

		boolean finEvoFlg = (boolean) iv.get(ParamsEnum.finEvo);
		if (finEvoFlg && !evolutionInfo.isFinalEvolution(iv.getGoPokedex().getPokedexId())) {
			res.setMessage("最終進化でないポケモンがヒットしたため、最終進化の絞り込みは実行されませんでした。\n");
			res.setMsgLevel(MsgLevelEnum.warn);
			finEvoFlg = false;
		}

		String pokedexId = iv.getGoPokedex().getPokedexId();

		Pokedex pokedex = pokedexRepository.findById(pokedexId).get();
		GoPokedex goPokedex = iv.getGoPokedex();

		Race race = new Race(pokedex, goPokedex, typeMap);
		res.setRace(race);

		res.setTooStrong(tooStrongPokemonList.contains(pokedexId));

		// 統計情報
		PokemonStatisticsInfo statistics;
		if (finEvoFlg) {
			// 最終進化のみで絞り込む場合は、最終進化用の統計情報を再生成する。
			Set<String> finEvoSet = evolutionInfo.getFinalEvoSet();
			statistics = new PokemonStatisticsInfo(
					pokedexRepository.findAllById(finEvoSet),
					goPokedexRepository.findAllById(finEvoSet));
		} else {
			// 全ポケモンを対象の統計情報はDIにある。
			statistics = pokemonStatisticsInfo.clone();
		}
		res.setStatistics(statistics);
	}

}
