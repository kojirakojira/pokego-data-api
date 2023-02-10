package jp.brainjuice.pokego.business.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.ibm.icu.text.MessageFormat;

import jp.brainjuice.pokego.business.dao.GoPokedexRepository;
import jp.brainjuice.pokego.business.dao.PokedexRepository;
import jp.brainjuice.pokego.business.dao.entity.GoPokedex;
import jp.brainjuice.pokego.business.dao.entity.Pokedex;
import jp.brainjuice.pokego.business.service.utils.dto.MultiSearchResult;
import jp.brainjuice.pokego.business.service.utils.memory.PokemonStatisticsInfo;
import jp.brainjuice.pokego.web.form.req.RaceDiffRequest;
import jp.brainjuice.pokego.web.form.res.MsgLevelEnum;
import jp.brainjuice.pokego.web.form.res.RaceDiffResponse;
import jp.brainjuice.pokego.web.form.res.elem.Race;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RaceDiffService {

	private PokedexRepository pokedexRepository;

	private GoPokedexRepository goPokedexRepository;

	private PokemonStatisticsInfo pokemonStatisticsInfo;

	private static final String MSG_NO_RESULTS = "存在しないIDが指定されました。";

	@Autowired
	public RaceDiffService(
			PokedexRepository pokedexRepository,
			GoPokedexRepository goPokedexRepository,
			PokemonStatisticsInfo pokemonStatisticsInfo) {
		this.pokedexRepository = pokedexRepository;
		this.goPokedexRepository = goPokedexRepository;
		this.pokemonStatisticsInfo = pokemonStatisticsInfo;
	}

	public boolean check(RaceDiffRequest req, RaceDiffResponse res) {

		if (req.getIdArr() == null && req.getNameArr() == null) {
			res.setSuccess(false);
			res.setMessage("idとnameの少なくとも一方は指定してください。。");
			res.setMsgLevel(MsgLevelEnum.error);
			return false;
		}

		if (req.getIdArr() != null && !(2 <= req.getIdArr().size() && req.getIdArr().size() <= 6)) {
			res.setSuccess(false);
			res.setMessage("idは2～6で指定してください。");
			res.setMsgLevel(MsgLevelEnum.error);
			return false;
		}

		if (req.getNameArr() != null && !(2 <= req.getNameArr().size() && req.getNameArr().size() <= 6)) {
			res.setSuccess(false);
			res.setMessage("nameは2～6で指定してください。");
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

		List<GoPokedex> goPokedexList = (List<GoPokedex>) goPokedexRepository.findAllById(req.getIdArr());

		if (CollectionUtils.contains(goPokedexList.iterator(), null)) {
			// 検索結果にnullが含まれている場合。
			res.setSuccess(false);
			res.setMessage(MSG_NO_RESULTS);
			res.setMsgLevel(MsgLevelEnum.error);
			log.info(MessageFormat.format(
					MSG_NO_RESULTS + " idList: {0}, goPokedexList: {1}",
					req.getIdArr().toString(),
					goPokedexList.toString()));
			return;
		}

		exec(goPokedexList, req.getIdArr(), res);
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
	}

	private void exec(List<GoPokedex> goPokedexList, List<String> idList, RaceDiffResponse res) {

		List<Pokedex> pokedexList = (List<Pokedex>) pokedexRepository.findAllById(idList);

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
