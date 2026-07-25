package jp.brainjuice.pokego.business.service.search.race;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import jp.brainjuice.pokego.business.service.search.general.PokemonSearchService;
import jp.brainjuice.pokego.business.service.search.utils.RaceDiffUtils;
import jp.brainjuice.pokego.business.service.search.utils.dto.MultiSearchResult;
import jp.brainjuice.pokego.business.service.search.utils.dto.PokemonSearchResult;
import jp.brainjuice.pokego.dao.jpa.GoPokedexRepository;
import jp.brainjuice.pokego.dao.jpa.entity.GoPokedex;
import jp.brainjuice.pokego.utils.exception.BadRequestException;
import jp.brainjuice.pokego.utils.exception.ProgramException;
import jp.brainjuice.pokego.web.search.form.req.race.RaceDiffRequest;
import jp.brainjuice.pokego.web.search.form.res.MsgLevelEnum;
import jp.brainjuice.pokego.web.search.form.res.elem.PidAndName;
import jp.brainjuice.pokego.web.search.form.res.race.RaceDiffResponse;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RaceDiffService {

	private GoPokedexRepository goPokedexRepository;

	private PokemonSearchService pokemonSearchService;

	private RaceDiffUtils raceDiffUtils;

	private static final String MSG_NO_UNIQUE_NAMES = "指定されたポケモンに重複があります。";

	private static final String MSG_NO_UNIQUE_NAME = "重複があります。";

	public RaceDiffService(
			GoPokedexRepository goPokedexRepository,
			PokemonSearchService pokemonSearchService,
			RaceDiffUtils raceDiffUtils) {
		this.goPokedexRepository = goPokedexRepository;
		this.pokemonSearchService = pokemonSearchService;
		this.raceDiffUtils = raceDiffUtils;
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

		// 重複チェック
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
								psr.setMessage(MSG_NO_UNIQUE_NAME);
							}
							return psr;
						})
						.toList());
				res.setMsr(msr);
				res.setMsgLevel(MsgLevelEnum.error);
				res.setMessage(MSG_NO_UNIQUE_NAMES);
				res.setSuccess(false);
				return false;
			}
		}

		return true;
	}

	private MultiSearchResult createUniqueMsr(List<PidAndName> pidAndNameList, List<String> pidList) {

		MultiSearchResult msr = new MultiSearchResult();
		List<GoPokedex> goPokedexList = goPokedexRepository.findAllById(Objects.requireNonNull(pidList));
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

		List<GoPokedex> goPokedexList = goPokedexRepository.findAllById(Objects.requireNonNull(idList));

		res.setRaceDiffResult(raceDiffUtils.createRaceDiffResult(goPokedexList, idList, true));
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

		res.setRaceDiffResult(raceDiffUtils.createRaceDiffResult(goPokedexList, idList, true));
		res.setSearchedById(false);
	}

}
