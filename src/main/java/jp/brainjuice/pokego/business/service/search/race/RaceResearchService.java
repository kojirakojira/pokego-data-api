package jp.brainjuice.pokego.business.service.search.race;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import jp.brainjuice.pokego.business.service.search.ResearchService;
import jp.brainjuice.pokego.business.service.search.pokeFilter.FilterEnum;
import jp.brainjuice.pokego.business.service.search.pokeFilter.GoPokedexFilterService;
import jp.brainjuice.pokego.business.service.search.pokeFilter.PokemonFilterValueUtils;
import jp.brainjuice.pokego.business.service.search.pokeFilter.dto.SearchValue;
import jp.brainjuice.pokego.business.service.search.pokeFilter.dto.SearchValue.ParamsEnum;
import jp.brainjuice.pokego.business.service.search.utils.PokemonUtils;
import jp.brainjuice.pokego.cache.inmemory.PokemonStatisticsInfo;
import jp.brainjuice.pokego.cache.inmemory.RaceExceptionsMap;
import jp.brainjuice.pokego.dao.jpa.GoPokedexRepository;
import jp.brainjuice.pokego.dao.jpa.PokedexRepository;
import jp.brainjuice.pokego.dao.jpa.dto.FilterParam;
import jp.brainjuice.pokego.dao.jpa.entity.GoPokedex;
import jp.brainjuice.pokego.dao.jpa.entity.Pokedex;
import jp.brainjuice.pokego.web.search.form.res.MsgLevelEnum;
import jp.brainjuice.pokego.web.search.form.res.elem.Race;
import jp.brainjuice.pokego.web.search.form.res.race.RaceResponse;

@Service
public class RaceResearchService implements ResearchService<RaceResponse> {

	private PokedexRepository pokedexRepository;

	private GoPokedexRepository goPokedexRepository;

	private PokemonStatisticsInfo pokemonStatisticsInfo;

	private GoPokedexFilterService goPokedexFilterService;

	private RaceExceptionsMap raceExceptionsMap;

	private PokemonUtils pokemonUtils;

	public RaceResearchService(
			PokedexRepository pokedexRepository,
			GoPokedexRepository goPokedexRepository,
			PokemonStatisticsInfo pokemonStatisticsInfo,
			GoPokedexFilterService goPokedexFilterService,
			RaceExceptionsMap raceExceptionsMap,
			PokemonUtils pokemonUtils) {
		this.pokedexRepository = pokedexRepository;
		this.goPokedexRepository = goPokedexRepository;
		this.pokemonStatisticsInfo = pokemonStatisticsInfo;
		this.goPokedexFilterService = goPokedexFilterService;
		this.raceExceptionsMap = raceExceptionsMap;
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

		res.setTooStrong(goPokedex.isTooStrong());

		// 絞り込み検索
		Map<FilterEnum, FilterParam> filterMap = PokemonFilterValueUtils.mapping(sv.getFilterValue());
		res.setFilteredItems(PokemonFilterValueUtils.convDisp(filterMap));

		// 絞り込み検索の実行有無
		boolean included = true;
		List<String> filterList = null;
		if (filterMap.size() > 0) {
			filterList = goPokedexFilterService.findIdByAny(filterMap);
			included = filterList.contains(pokedexId);
			if (!included) {
				// 絞り込みがおこなわれている場合、かつ検索したポケモンが絞り込み後のポケモンにいない場合
				res.setMessage("選択したポケモンが絞り込み条件の対象外でした。絞り込みは実行されませんでした。\n");
				res.setMsgLevel(MsgLevelEnum.warn);
			}
		}
		res.setIncluded(included);

		// ポケモン統計情報
		PokemonStatisticsInfo statistics;
		if (filterMap.size() > 0 && filterList.contains(pokedexId)) {
			// 絞り込みがおこなわれている場合、かつ検索したポケモンが絞り込み後のポケモンにいる場合

			// 最終進化のみで絞り込む場合は、最終進化用の統計情報を再生成する。
			statistics = new PokemonStatisticsInfo(
					pokedexRepository.findAllById(filterList),
					goPokedexRepository.findAllById(filterList),
					raceExceptionsMap);
		} else {
			// 全ポケモンを対象の統計情報はDIにある。
			statistics = pokemonStatisticsInfo.clone();
		}
		if (sv.get(ParamsEnum.statsRequired, boolean.class)) {
			res.setStatistics(statistics);
		}

		Race race = new Race(pokedex, goPokedex, statistics);
		res.setRace(race);

		res.setGoTotalCount(statistics.getGoPokedexStats().getGoHpStats().getList().size());
		res.setOriTotalCount(statistics.getPokedexStats().getHpStats().getList().size());
	}

}
