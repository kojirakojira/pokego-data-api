package jp.brainjuice.pokego.business.service.race;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import jp.brainjuice.pokego.business.dao.GoPokedexRepository;
import jp.brainjuice.pokego.business.dao.PokedexRepository;
import jp.brainjuice.pokego.business.dao.entity.GoPokedex;
import jp.brainjuice.pokego.business.dao.entity.Pokedex;
import jp.brainjuice.pokego.business.service.general.PokemonSearchService;
import jp.brainjuice.pokego.business.service.utils.PokemonUtils;
import jp.brainjuice.pokego.business.service.utils.dto.MultiSearchResult;
import jp.brainjuice.pokego.business.service.utils.dto.PokemonSearchResult;
import jp.brainjuice.pokego.cache.inmemory.PokemonStatisticsInfo;
import jp.brainjuice.pokego.utils.exception.BadRequestException;
import jp.brainjuice.pokego.utils.exception.ProgramException;
import jp.brainjuice.pokego.web.form.req.race.RaceDiffRequest;
import jp.brainjuice.pokego.web.form.res.MsgLevelEnum;
import jp.brainjuice.pokego.web.form.res.elem.PidAndName;
import jp.brainjuice.pokego.web.form.res.elem.Race;
import jp.brainjuice.pokego.web.form.res.race.RaceDiffResponse;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RaceDiffService {

	private PokedexRepository pokedexRepository;

	private GoPokedexRepository goPokedexRepository;

	private PokemonStatisticsInfo pokemonStatisticsInfo;
	
	private PokemonSearchService pokemonSearchService;

	private PokemonUtils pokemonUtils;

	private static final String MSG_NO_RESULTS = "存在しないIDが指定されました。";
	
	private static final String MSG_NO_UNIQUE_NAMES = "ポケモン名はユニークになるように指定してください。";

	public RaceDiffService(
			PokedexRepository pokedexRepository,
			GoPokedexRepository goPokedexRepository,
			PokemonStatisticsInfo pokemonStatisticsInfo,
			PokemonSearchService pokemonSearchService,
			PokemonUtils pokemonUtils) {
		this.pokedexRepository = pokedexRepository;
		this.goPokedexRepository = goPokedexRepository;
		this.pokemonStatisticsInfo = pokemonStatisticsInfo;
		this.pokemonSearchService = pokemonSearchService;
		this.pokemonUtils = pokemonUtils;
	}

	public boolean checkBeforeNameSearch(RaceDiffRequest req, RaceDiffResponse res) throws BadRequestException {

		List<PidAndName> pidAndNameList = req.getPidAndNameArr();
		
		if (pidAndNameList == null) {
			throw new BadRequestException();
		}
		
		int pidSize = (int) pidAndNameList.stream()
				.map(PidAndName::getPid)
				.filter(StringUtils::isNotEmpty)
				.count();
		int nameSize = (int) pidAndNameList.stream()
				.map(PidAndName::getName)
				.filter(StringUtils::isNotEmpty)
				.count();

		if (pidSize == 0 && nameSize == 0) {
			res.setSuccess(false);
			res.setMessage("pidとnameの少なくとも一方は指定してください。");
			res.setMsgLevel(MsgLevelEnum.error);
			return false;
		}

		int pidAndNameListSize = pidAndNameList.size();
		if (pidAndNameListSize < 2 || 6 < pidAndNameListSize) {
			res.setSuccess(false);
			res.setMessage("pidまたはnameを2～6個指定してください。");
			res.setMsgLevel(MsgLevelEnum.error);
			return false;
		}

		return true;
	}

	/**
	 * id検索直前のチェック
	 * 
	 * @param req
	 * @param res
	 * @return
	 * @throws BadRequestException
	 */
	public boolean checkBeforeIdSearch(RaceDiffRequest req, RaceDiffResponse res) throws BadRequestException {

		List<PidAndName> pidAndNameList = req.getPidAndNameArr();

		//  重複チェック
		// id検索とname検索の二軸両方を処理している。正直見にくい…。
		{
			List<String> pidList;
			if (!pidAndNameList.stream()
					.filter(pan -> StringUtils.isEmpty(pan.getPid()))
					.anyMatch(e -> true)) {
				// pidがすべて揃っている場合(このリクエストがid検索として呼ばれた場合)
				pidList = pidAndNameList.stream()
						.map(PidAndName::getPid)
						.toList();
			} else {
				// pidが揃っていない場合
				// name検索が済んでいるはずである
				MultiSearchResult msr = res.getMsr();
				if (msr.getPsrArr().stream()
						.filter(psr -> psr.getGoPokedex() == null)
						.anyMatch(e -> true)) {
					// PokemonSearchResultがすべて揃っていない場合はプログラムに問題がある
					throw new ProgramException();
				}

				pidList = msr.getPsrArr().stream()
						.map(psr -> psr.getGoPokedex().getPokedexId())
						.toList();
			}

			// 重複があるpokedexIdのリスト
			List<String> duplicateList = pidList.stream()
					.collect(Collectors.groupingBy(pid -> pid))
					.entrySet().stream()
					.filter(entry -> 1 < entry.getValue().size())
					.map(Map.Entry::getKey)
					.toList();
			if (0 < duplicateList.size()) {
				// 重複ありの場合
				MultiSearchResult msr = res.getMsr();

				if (msr == null) {
					// id検索の場合、MultiSearchResultはnull
					// id検索かどうかでも判定できるが、一応msrがnullかどうかで判定しておく
					msr = createUniqueMsr(pidAndNameList, pidList);
				}
				// 重複がある行にメッセージを設定
				msr.setPsrArr(msr.getPsrArr().stream()
						.map(psr -> {
							if (duplicateList.contains(psr.getGoPokedex().getPokedexId())) {
								psr.setMsgLevel(MsgLevelEnum.error);
								psr.setMessage("重複があります。");
							}
							return psr;
						})
						.toList());
				res.setMsr(msr);
				res.setMsgLevel(MsgLevelEnum.error);
				res.setMessage("指定されたポケモンに重複があります。");
				res.setSuccess(false);
				return false;
			}
		}

		return true;
	}
	
	private MultiSearchResult createUniqueMsr(List<PidAndName> pidAndNameList, List<String> pidList) {

		MultiSearchResult msr = new MultiSearchResult();
		List<GoPokedex> goPokedexList = goPokedexRepository.findAllById(pidList);
		List<PokemonSearchResult> psrList = pidAndNameList.stream()
				.map(PidAndName::getPid)
				.map(pid -> goPokedexList.stream()
						.filter(gp -> pid.equals(gp.getPokedexId()))
						.findFirst().orElseThrow())
				.map(gp -> pokemonSearchService.createUniquePsr(gp))
				.toList();
		msr.setAllUnique(true);
		msr.setPsrArr(psrList);
		
		return msr;
	}

	/**
	 * idから検索する場合の受け口
	 *
	 * @param req
	 * @param res
	 */
	public void exec(RaceDiffRequest req, RaceDiffResponse res) {

		List<String> idList = req.getPidAndNameArr().stream()
				.map(PidAndName::getPid)
				.toList();
		
		List<GoPokedex> goPokedexList = (List<GoPokedex>) goPokedexRepository.findAllById(idList);

		exec(goPokedexList, idList, res);
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

		List<GoPokedex> goPokedexList = msr.getPsrArr().stream()
				.map(p -> p.getGoPokedex())
				.toList();
		List<String> idList = goPokedexList.stream()
				.map(gp -> gp.getPokedexId())
				.toList();

		exec(goPokedexList, idList, res);
		res.setSearchedById(false);
	}

	/**
	 * 主処理
	 *
	 * @param goPokedexList
	 * @param idList
	 * @param res
	 */
	private void exec(List<GoPokedex> goPokedexList, List<String> idList, RaceDiffResponse res) {

		final List<Pokedex> pokedexList = (List<Pokedex>) pokedexRepository.findAllById(idList);
		
		List<Race> raceList = idList.stream() // 検索した時のid順で作成
				.map(pid -> {
					Pokedex p = pokedexList.stream()
							.filter(pdx -> pdx.getPokedexId().equals(pid))
							.findFirst().get();
					// 原作種族値が存在しない場合は、Pokedexをnullにする。
					p = pokemonUtils.existsOrigin(p.getPokedexId()) ? p : null;
					
					GoPokedex gp = goPokedexList.stream()
							.filter(gPdx -> gPdx.getPokedexId().equals(pid))
							.findFirst().get();
					return new Race(p, gp, pokemonStatisticsInfo);
				})
				.toList();
		res.setRaceArr(raceList);

		res.setGoTotalCount(pokemonStatisticsInfo.getGoPokedexStats().getGoHpStats().getList().size());
		res.setOriTotalCount(pokemonStatisticsInfo.getPokedexStats().getHpStats().getList().size());;

		res.setSuccess(true);
	}

}
