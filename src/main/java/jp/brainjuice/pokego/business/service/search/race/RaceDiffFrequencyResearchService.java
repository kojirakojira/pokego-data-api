package jp.brainjuice.pokego.business.service.search.race;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import jp.brainjuice.pokego.business.service.search.ResearchService;
import jp.brainjuice.pokego.business.service.search.pokeFilter.dto.SearchValue;
import jp.brainjuice.pokego.business.service.search.utils.RaceDiffUtils;
import jp.brainjuice.pokego.business.service.search.utils.dto.raceDiff.RaceDiffResult;
import jp.brainjuice.pokego.dao.jpa.GoPokedexRepository;
import jp.brainjuice.pokego.dao.jpa.RaceDiffSearchHistoryRepository;
import jp.brainjuice.pokego.dao.jpa.entity.GoPokedex;
import jp.brainjuice.pokego.dao.jpa.entity.RaceDiffSearchHistory;
import jp.brainjuice.pokego.web.search.form.res.MsgLevelEnum;
import jp.brainjuice.pokego.web.search.form.res.race.RaceDiffFrequencyResponse;

@Service
public class RaceDiffFrequencyResearchService implements ResearchService<RaceDiffFrequencyResponse> {

	private RaceDiffSearchHistoryRepository raceDiffSearchHistoryRepository;

	private GoPokedexRepository goPokedexRepository;

	private RaceDiffUtils raceDiffUtils;

	public RaceDiffFrequencyResearchService(
			RaceDiffSearchHistoryRepository raceDiffSearchHistoryRepository,
			GoPokedexRepository goPokedexRepository,
			RaceDiffUtils raceDiffUtils) {
		this.raceDiffSearchHistoryRepository = raceDiffSearchHistoryRepository;
		this.goPokedexRepository = goPokedexRepository;
		this.raceDiffUtils = raceDiffUtils;
	}

	/**
	 * 検索履歴から最も比較されたポケモンの情報を取得する
	 */
	@Override
	public void exec(SearchValue sv, RaceDiffFrequencyResponse res) {

		String pokedexId = sv.getGoPokedex().getPokedexId();

		if (StringUtils.isEmpty(pokedexId)) {
			res.setSuccess(false);
			res.setMessage("pokedexIdが指定されていません。");
			res.setMsgLevel(MsgLevelEnum.error);
			return;
		}

		RaceDiffSearchHistory history = raceDiffSearchHistoryRepository.findMostFrequentByPid(pokedexId);

		if (history == null) {
			// 履歴がない場合は空で返す。
			res.setRaceDiffElemArr(List.of());
			return;
		}

		List<String> idList = new java.util.ArrayList<>();
		if (StringUtils.isNotEmpty(history.getPokedexId1()))
			idList.add(history.getPokedexId1());
		if (StringUtils.isNotEmpty(history.getPokedexId2()))
			idList.add(history.getPokedexId2());
		if (StringUtils.isNotEmpty(history.getPokedexId3()))
			idList.add(history.getPokedexId3());
		if (StringUtils.isNotEmpty(history.getPokedexId4()))
			idList.add(history.getPokedexId4());
		if (StringUtils.isNotEmpty(history.getPokedexId5()))
			idList.add(history.getPokedexId5());
		if (StringUtils.isNotEmpty(history.getPokedexId6()))
			idList.add(history.getPokedexId6());

		List<GoPokedex> goPokedexList = (List<GoPokedex>) goPokedexRepository.findAllById(idList);

		// idListの順序を維持してexecメソッドを呼び出す
		List<GoPokedex> orderedGoPokedexList = idList.stream()
				.map(id -> goPokedexList.stream().filter(gp -> gp.getPokedexId().equals(id)).findFirst().orElse(null))
				.filter(java.util.Objects::nonNull)
				.toList();

		RaceDiffResult result = raceDiffUtils.createRaceDiffResult(orderedGoPokedexList, idList, false);
		res.setRaceDiffResult(result);
	}

}
