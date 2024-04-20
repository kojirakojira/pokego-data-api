package jp.brainjuice.pokego.business.service.race;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jp.brainjuice.pokego.business.dao.GoPokedexRepository;
import jp.brainjuice.pokego.business.dao.PokedexRepository;
import jp.brainjuice.pokego.business.dao.PokedexSpecifications.FilterEnum;
import jp.brainjuice.pokego.business.dao.dto.FilterParam;
import jp.brainjuice.pokego.business.dao.entity.GoPokedex;
import jp.brainjuice.pokego.business.dao.entity.Pokedex;
import jp.brainjuice.pokego.business.service.ResearchService;
import jp.brainjuice.pokego.business.service.utils.PokemonFilterValueUtils;
import jp.brainjuice.pokego.business.service.utils.PokemonUtils;
import jp.brainjuice.pokego.business.service.utils.dto.SearchValue;
import jp.brainjuice.pokego.business.service.utils.memory.PokemonStatisticsInfo;
import jp.brainjuice.pokego.business.service.utils.memory.TooStrongPokemonList;
import jp.brainjuice.pokego.web.form.res.MsgLevelEnum;
import jp.brainjuice.pokego.web.form.res.elem.Race;
import jp.brainjuice.pokego.web.form.res.race.RaceResponse;

@Service
public class RaceResearchService implements ResearchService<RaceResponse> {

	private PokedexRepository pokedexRepository;

	private GoPokedexRepository goPokedexRepository;

	private PokemonStatisticsInfo pokemonStatisticsInfo;

	private TooStrongPokemonList tooStrongPokemonList;

	private PokemonUtils pokemonUtils;

	@Autowired
	public RaceResearchService(
			PokedexRepository pokedexRepository,
			GoPokedexRepository goPokedexRepository,
			PokemonStatisticsInfo pokemonStatisticsInfo,
			TooStrongPokemonList tooStrongPokemonList,
			PokemonUtils pokemonUtils) {
		this.pokedexRepository = pokedexRepository;
		this.goPokedexRepository = goPokedexRepository;
		this.pokemonStatisticsInfo = pokemonStatisticsInfo;
		this.tooStrongPokemonList = tooStrongPokemonList;
		this.pokemonUtils = pokemonUtils;
	}

	@Override
	public void exec(SearchValue sv, RaceResponse res) {

		String pokedexId = sv.getGoPokedex().getPokedexId();

		Pokedex pokedex = null;
		if (pokemonUtils.existsOrigin(pokedexId)) {
			pokedex = pokedexRepository.findById(pokedexId).get();
		}
		GoPokedex goPokedex = sv.getGoPokedex();

		Race race = new Race(pokedex, goPokedex);
		res.setRace(race);

		res.setTooStrong(tooStrongPokemonList.contains(pokedexId));

		// 絞り込み検索
		Map<FilterEnum, FilterParam> filterMap = PokemonFilterValueUtils.mapping(sv.getFilterValue());
		res.setFilteredItems(PokemonFilterValueUtils.convDisp(filterMap));

		// 絞り込み検索の実行有無
		List<String> filterList = goPokedexRepository.findIdByAny(filterMap);
		int pokedexCnt = (int) goPokedexRepository.count();
		boolean included = filterList.size() == pokedexCnt || filterList.contains(pokedexId);
		if (!included) {
			// 絞り込みがおこなわれている場合、かつ検索したポケモンが絞り込み後のポケモンにいない場合
			res.setMessage("選択したポケモンが絞り込み条件の対象外でした。絞り込みは実行されませんでした。\n");
			res.setMsgLevel(MsgLevelEnum.warn);
		}
		res.setIncluded(included);

		// 統計情報
		PokemonStatisticsInfo statistics;
		if (filterList.size() != pokedexCnt && filterList.contains(pokedexId)) {
			// 絞り込みがおこなわれている場合、かつ検索したポケモンが絞り込み後のポケモンにいる場合

			// 最終進化のみで絞り込む場合は、最終進化用の統計情報を再生成する。
			statistics = new PokemonStatisticsInfo(
					pokedexRepository.findAllById(filterList),
					goPokedexRepository.findAllById(filterList));
		} else {
			// 全ポケモンを対象の統計情報はDIにある。
			statistics = pokemonStatisticsInfo.clone();
		}
		res.setStatistics(statistics);
	}

}
