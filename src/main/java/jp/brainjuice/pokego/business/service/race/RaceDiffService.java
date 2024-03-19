package jp.brainjuice.pokego.business.service.race;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import jp.brainjuice.pokego.business.dao.GoPokedexRepository;
import jp.brainjuice.pokego.business.dao.PokedexRepository;
import jp.brainjuice.pokego.business.dao.entity.GoPokedex;
import jp.brainjuice.pokego.business.dao.entity.Pokedex;
import jp.brainjuice.pokego.business.service.utils.PokemonUtils;
import jp.brainjuice.pokego.business.service.utils.dto.MultiSearchResult;
import jp.brainjuice.pokego.business.service.utils.memory.PokemonStatisticsInfo;
import jp.brainjuice.pokego.web.form.req.race.RaceDiffRequest;
import jp.brainjuice.pokego.web.form.res.MsgLevelEnum;
import jp.brainjuice.pokego.web.form.res.elem.Race;
import jp.brainjuice.pokego.web.form.res.race.RaceDiffResponse;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RaceDiffService {

	private PokedexRepository pokedexRepository;

	private GoPokedexRepository goPokedexRepository;

	private PokemonStatisticsInfo pokemonStatisticsInfo;

	private PokemonUtils pokemonUtils;

	private static final String MSG_NO_RESULTS = "存在しないIDが指定されました。";

	@Autowired
	public RaceDiffService(
			PokedexRepository pokedexRepository,
			GoPokedexRepository goPokedexRepository,
			PokemonStatisticsInfo pokemonStatisticsInfo,
			PokemonUtils pokemonUtils) {
		this.pokedexRepository = pokedexRepository;
		this.goPokedexRepository = goPokedexRepository;
		this.pokemonStatisticsInfo = pokemonStatisticsInfo;
		this.pokemonUtils = pokemonUtils;
	}

	public boolean check(RaceDiffRequest req, RaceDiffResponse res) {

		if (req.getPidArr() == null && req.getNameArr() == null) {
			res.setSuccess(false);
			res.setMessage("idとnameの少なくとも一方は指定してください。");
			res.setMsgLevel(MsgLevelEnum.error);
			return false;
		}

		if (req.getPidArr() != null && !(2 <= req.getPidArr().size() && req.getPidArr().size() <= 6)) {
			res.setSuccess(false);
			res.setMessage("idは2～6個で指定してください。");
			res.setMsgLevel(MsgLevelEnum.error);
			return false;
		}

		if (req.getNameArr() != null && !(2 <= req.getNameArr().size() && req.getNameArr().size() <= 6)) {
			res.setSuccess(false);
			res.setMessage("nameは2～6個で指定してください。");
			res.setMsgLevel(MsgLevelEnum.error);
			return false;
		}

		return true;
	}

	/**
	 * idから検索する場合の受け口
	 *
	 * @param req
	 * @param res
	 */
	public void exec(RaceDiffRequest req, RaceDiffResponse res) {

		List<GoPokedex> goPokedexList = (List<GoPokedex>) goPokedexRepository.findAllById(req.getPidArr());

		if (CollectionUtils.contains(goPokedexList.iterator(), null)) {
			// 検索結果にnullが含まれている場合。
			res.setSuccess(false);
			res.setMessage(MSG_NO_RESULTS);
			res.setMsgLevel(MsgLevelEnum.error);
			log.info(MessageFormat.format(
					MSG_NO_RESULTS + " idList: {0}, goPokedexList: {1}",
					req.getPidArr().toString(),
					goPokedexList.toString()));
			return;
		}

		exec(goPokedexList, req.getPidArr(), res);
		res.setSearchedById(true);
	}

	/**
	 * nameから検索する場合の受け口
	 *
	 * @param req
	 * @param res
	 * @param msr
	 */
	public void exec(MultiSearchResult msr, RaceDiffResponse res) {

		if (!msr.isAllUnique()) {
			log.warn("MultiSearchResultのPokemonSearchResultは、検索結果がすべてユニークでなければなりません。");
			return;
		}

		List<GoPokedex> goPokedexList = msr.getPsrArr().stream().map(p -> p.getGoPokedex()).collect(Collectors.toList());
		List<String> idList = goPokedexList.stream().map(gp -> gp.getPokedexId()).collect(Collectors.toList());

		exec(goPokedexList, idList, res);
		res.setSearchedById(false);
	}

	/**
	 * 当サービスの主処理
	 *
	 * @param goPokedexList
	 * @param idList
	 * @param res
	 */
	private void exec(List<GoPokedex> goPokedexList, List<String> idList, RaceDiffResponse res) {

		List<Pokedex> pokedexList = (List<Pokedex>) pokedexRepository.findAllById(idList);
		// 原作種族値が存在しない場合は、Pokedexをnullにする。
		pokedexList = pokedexList.stream()
				.map(p -> pokemonUtils.existsOrigin(p.getPokedexId()) ? p : null)
				.collect(Collectors.toList());

		List<Race> raceList = new ArrayList<>();
		for (int i = 0; i < idList.size(); i++) {
			Race race = new Race(pokedexList.get(i), goPokedexList.get(i));
			raceList.add(race);
		}
		res.setRaceArr(raceList);

		res.setStatistics(pokemonStatisticsInfo.clone());

		res.setSuccess(true);
	}

}
