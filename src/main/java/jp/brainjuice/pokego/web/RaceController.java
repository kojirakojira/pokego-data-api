package jp.brainjuice.pokego.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jp.brainjuice.pokego.business.service.ResearchServiceExecutor;
import jp.brainjuice.pokego.business.service.general.PokemonSearchService;
import jp.brainjuice.pokego.business.service.race.RaceDiffService;
import jp.brainjuice.pokego.business.service.race.RaceResearchService;
import jp.brainjuice.pokego.business.service.utils.dto.MultiSearchResult;
import jp.brainjuice.pokego.cache.service.ViewsCacheProvider;
import jp.brainjuice.pokego.utils.exception.BadRequestException;
import jp.brainjuice.pokego.web.form.req.race.RaceDiffRequest;
import jp.brainjuice.pokego.web.form.req.race.RaceRequest;
import jp.brainjuice.pokego.web.form.res.race.RaceDiffResponse;
import jp.brainjuice.pokego.web.form.res.race.RaceResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * 種族値情報を取得するためのコントローラクラス
 *
 * @author saibabanagchampa
 *
 */
@RestController
@RequestMapping("/api")
@Slf4j
public class RaceController {

	private PokemonSearchService pokemonSearchService;

	private RaceResearchService raceResearchService;
	private ResearchServiceExecutor<RaceResponse> raceResRse;

	private RaceDiffService raceDiffService;

	private ViewsCacheProvider viewsCacheProvider;

	public RaceController(
			RaceDiffService raceDiffService,
			RaceResearchService raceResearchService, ResearchServiceExecutor<RaceResponse> raceResRse,
			PokemonSearchService pokemonSearchService,
			ViewsCacheProvider viewsCacheProvider) {

		// 検索
		this.pokemonSearchService = pokemonSearchService;

		// 種族値検索
		this.raceResearchService = raceResearchService;
		this.raceResRse = raceResRse;

		// 種族値比較
		this.raceDiffService = raceDiffService;

		this.viewsCacheProvider = viewsCacheProvider;

	}

	/**
	 * 種族値検索用API
	 *
	 * @param raceReq
	 * @return
	 * @throws BadRequestException
	 */
	@GetMapping("/race")
	public RaceResponse race(RaceRequest raceReq) throws BadRequestException {

		RaceResponse raceRes = new RaceResponse();
		raceResRse.execute(raceReq, raceRes, raceResearchService);
		return raceRes;
	}

	/**
	 * 種族値比較用API（content-type:application/jsonで取得する。）
	 *
	 * @param raceReq
	 * @return
	 * @throws BadRequestException
	 */
	@GetMapping("/raceDiff")
	public RaceDiffResponse raceDiff(RaceDiffRequest raceDiffReq) throws BadRequestException {

		RaceDiffResponse raceDiffRes = new RaceDiffResponse();
		if (!raceDiffService.check(raceDiffReq, raceDiffRes)) {
			return raceDiffRes;
		}

		if (raceDiffReq.getIdArr() != null) {
			// idでの検索
			raceDiffService.exec(raceDiffReq, raceDiffRes);

		} else {

			MultiSearchResult msr = pokemonSearchService.multiSearch(raceDiffReq.getNameArr());
			raceDiffRes.setMsr(msr);
			raceDiffRes.setSuccess(true);

			if (msr.isAllUnique()) {
				// MultiSearchResult
				raceDiffService.exec(msr, raceDiffRes);
			}
		}

		// 閲覧数を手動で追加。
		viewsCacheProvider.addTempList();

		return raceDiffRes;
	}


	@ExceptionHandler(BadRequestException.class)
	public ResponseEntity<String> badRequestException(Exception e) {
		String errMsg = "不正なリクエストです。";
		log.error(errMsg, e);
		return new ResponseEntity<String>(errMsg, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<String> exception(Exception e) {
		String errMsg = "処理中に想定外の問題が発生しました。";
		log.error(errMsg, e);
		return new ResponseEntity<String>(errMsg, HttpStatus.INTERNAL_SERVER_ERROR);
	}

}
