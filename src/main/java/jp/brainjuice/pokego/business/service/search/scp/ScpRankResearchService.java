package jp.brainjuice.pokego.business.service.search.scp;

import java.util.List;

import org.springframework.stereotype.Service;

import jp.brainjuice.pokego.business.service.search.ResearchService;
import jp.brainjuice.pokego.business.service.search.pokeFilter.dto.SearchValue;
import jp.brainjuice.pokego.business.service.search.pokeFilter.dto.SearchValue.ParamsEnum;
import jp.brainjuice.pokego.business.service.search.utils.PokemonEditUtils;
import jp.brainjuice.pokego.business.service.search.utils.ScpRankCalculator;
import jp.brainjuice.pokego.business.service.search.utils.evo.EvolutionProvider;
import jp.brainjuice.pokego.dao.jpa.GoPokedexRepository;
import jp.brainjuice.pokego.dao.jpa.entity.Evolution;
import jp.brainjuice.pokego.dao.jpa.entity.GoPokedex;
import jp.brainjuice.pokego.web.search.form.res.elem.ScpRankAllInOne;
import jp.brainjuice.pokego.web.search.form.res.scp.ScpRankResponse;

@Service
public class ScpRankResearchService implements ResearchService<ScpRankResponse> {

	private ScpRankCalculator scpRankCalculator;

	private GoPokedexRepository goPokedexRepository;

	private EvolutionProvider evolutionProvider;

	public ScpRankResearchService(
			ScpRankCalculator scpRankCalculator,
			GoPokedexRepository goPokedexRepository,
			EvolutionProvider evolutionProvider) {
		this.scpRankCalculator = scpRankCalculator;
		this.goPokedexRepository = goPokedexRepository;
		this.evolutionProvider = evolutionProvider;
	}

	@Override
	public void exec(SearchValue sv, ScpRankResponse res) {

		int iva = sv.get(ParamsEnum.iva, int.class);
		int ivd = sv.get(ParamsEnum.ivd, int.class);
		int ivh = sv.get(ParamsEnum.ivh, int.class);

		GoPokedex goPokedex = sv.getGoPokedex();

		res.setTargetScpRank(createScpRankAllInOne(goPokedex, iva, ivd, ivh));
		
		// 同系統のすべてのポケモンを取得
		List<Evolution> lineageList = evolutionProvider.getLineageList(goPokedex);
		// 同系統のすべてのポケモンのGoPokedexを取得
		List<GoPokedex> gpList = goPokedexRepository.findAllById(
				lineageList.stream().map(Evolution::getPokedexId).toList()
				);
		// 進化後のポケモンを全て取得
		List<String> afEvolPidList = evolutionProvider.getAllAfterEvolution(goPokedex.getPokedexId(), lineageList);
		
		// 進化後のポケモン
		List<ScpRankAllInOne> afEvolScpRankList = gpList.stream()
				.filter(gp -> afEvolPidList.contains(gp.getPokedexId()))
				.filter(gp -> !goPokedex.getPokedexId().equals(gp.getPokedexId()))
				.sorted(PokemonEditUtils.getPokedexComparator(1))
				.map(gp -> createScpRankAllInOne(gp, iva, ivd, ivh))
				.toList();
		res.setAfEvolScpRankList(afEvolScpRankList);

		// 別のすがた
		List<ScpRankAllInOne> anotherFormScpRankList = gpList.stream()
				.filter(gp -> !afEvolPidList.contains(gp.getPokedexId()))
				.filter(gp -> !goPokedex.getPokedexId().equals(gp.getPokedexId()))
				.sorted(PokemonEditUtils.getPokedexComparator(1))
				.map(gp -> createScpRankAllInOne(gp, iva, ivd, ivh))
				.toList();
		res.setAnotherFormScpRankList(anotherFormScpRankList);
	}
	
	private ScpRankAllInOne createScpRankAllInOne(GoPokedex goPokedex, int iva, int ivd, int ivh) {
		return new ScpRankAllInOne(
				goPokedex,
				scpRankCalculator.getSuperLeagueRank(goPokedex, iva, ivd, ivh),
				scpRankCalculator.getHyperLeagueRank(goPokedex, iva, ivd, ivh),
				scpRankCalculator.getMasterLeagueRank(goPokedex, iva, ivd, ivh)
				);
	}

}
