package jp.brainjuice.pokego.business.service.research;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jp.brainjuice.pokego.business.dao.GoPokedexRepository;
import jp.brainjuice.pokego.business.dao.PokedexFilterInfoRepository;
import jp.brainjuice.pokego.business.dao.PokedexFilterInfoRepository.FilterEnum;
import jp.brainjuice.pokego.business.dao.PokedexRepository;
import jp.brainjuice.pokego.business.dao.dto.FilterParam;
import jp.brainjuice.pokego.business.dao.entity.GoPokedex;
import jp.brainjuice.pokego.business.dao.entity.Pokedex;
import jp.brainjuice.pokego.business.service.utils.IndividialValueUtils;
import jp.brainjuice.pokego.business.service.utils.dto.IndividialValue;
import jp.brainjuice.pokego.business.service.utils.memory.PokemonStatisticsInfo;
import jp.brainjuice.pokego.business.service.utils.memory.TooStrongPokemonList;
import jp.brainjuice.pokego.web.form.res.MsgLevelEnum;
import jp.brainjuice.pokego.web.form.res.elem.Race;
import jp.brainjuice.pokego.web.form.res.research.RaceResponse;

@Service
public class RaceResearchService implements ResearchService<RaceResponse> {

	private PokedexRepository pokedexRepository;

	private GoPokedexRepository goPokedexRepository;

	private PokedexFilterInfoRepository pokedexFilterInfoRepository;

	private PokemonStatisticsInfo pokemonStatisticsInfo;

	private TooStrongPokemonList tooStrongPokemonList;

	@Autowired
	public RaceResearchService(
			PokedexRepository pokedexRepository,
			GoPokedexRepository goPokedexRepository,
			PokedexFilterInfoRepository pokedexFilterInfoRepository,
			PokemonStatisticsInfo pokemonStatisticsInfo,
			TooStrongPokemonList tooStrongPokemonList) {
		this.pokedexRepository = pokedexRepository;
		this.goPokedexRepository = goPokedexRepository;
		this.pokedexFilterInfoRepository = pokedexFilterInfoRepository;
		this.pokemonStatisticsInfo = pokemonStatisticsInfo;
		this.tooStrongPokemonList = tooStrongPokemonList;
	}

	@Override
	public void exec(IndividialValue iv, RaceResponse res) {

		String pokedexId = iv.getGoPokedex().getPokedexId();

		Pokedex pokedex = pokedexRepository.findById(pokedexId).get();
		GoPokedex goPokedex = iv.getGoPokedex();

		Race race = new Race(pokedex, goPokedex);
		res.setRace(race);

		res.setTooStrong(tooStrongPokemonList.contains(pokedexId));

		// 絞り込み検索
		Map<FilterEnum, FilterParam> filterMap = IndividialValueUtils.mapping(iv.getFilterValue());
		res.setFilteredItems(IndividialValueUtils.convDisp(filterMap));

		// 絞り込み検索の実行有無
		List<String> filterList = pokedexFilterInfoRepository.findByAny(filterMap);
		int pokedexCnt = (int) pokedexFilterInfoRepository.count();
		if (filterList.size() != pokedexCnt && !filterList.contains(pokedexId)) {
			// 絞り込みがおこなわれている場合、かつ検索したポケモンが絞り込み後のポケモンにいない場合
			res.setMessage("選択したポケモンが絞り込み条件の対象外でした。絞り込みは実行されませんでした。\n");
			res.setMsgLevel(MsgLevelEnum.warn);
		}

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
