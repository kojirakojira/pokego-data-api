package jp.brainjuice.pokego.web;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jp.brainjuice.pokego.business.service.ResearchServiceExecutor;
import jp.brainjuice.pokego.business.service.others.EvoCostResearchService;
import jp.brainjuice.pokego.business.service.others.EvoCostResearchService.Costs;
import jp.brainjuice.pokego.business.service.others.EvolutionResearchService;
import jp.brainjuice.pokego.business.service.others.UnimplPokemonService;
import jp.brainjuice.pokego.business.service.utils.ValidationService;
import jp.brainjuice.pokego.cache.service.ViewsCacheProvider;
import jp.brainjuice.pokego.utils.exception.BadRequestException;
import jp.brainjuice.pokego.web.form.req.others.EvoCostRequest;
import jp.brainjuice.pokego.web.form.req.others.EvolutionRequest;
import jp.brainjuice.pokego.web.form.res.others.EvoCostResponse;
import jp.brainjuice.pokego.web.form.res.others.EvolutionResponse;
import jp.brainjuice.pokego.web.form.res.others.UnimplPokemonResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * その他機能のコントローラクラス
 *
 * @author saibabanagchampa
 *
 */
@RestController
@RequestMapping("/api")
@Slf4j
public class OthersResearchController {

	private EvolutionResearchService evolutionResearchService;
	private ResearchServiceExecutor<EvolutionResponse> evolutionResRse;

	private EvoCostResearchService evoCostResearchService;

	private UnimplPokemonService unimplPokemonService;

	private ValidationService validationService;

	private ViewsCacheProvider viewsCacheProvider;

	@Autowired
	public OthersResearchController (
			EvolutionResearchService evolutionResearchService, ResearchServiceExecutor<EvolutionResponse> evolutionResRse,
			EvoCostResearchService evoCostResearchService,
			UnimplPokemonService unimplPokemonService,
			ValidationService validationService,
			ViewsCacheProvider viewsCacheProvider) {

		// 進化ツリー
		this.evolutionResearchService = evolutionResearchService;
		this.evolutionResRse = evolutionResRse;

		// 進化コスト
		this.evoCostResearchService = evoCostResearchService;

		this.unimplPokemonService = unimplPokemonService;

		this.validationService = validationService;
		this.viewsCacheProvider = viewsCacheProvider;
	}

	/**
	 * 進化取得用API
	 *
	 * @param evolutionReq
	 * @return
	 * @throws BadRequestException
	 */
	@GetMapping("/evolution")
	public EvolutionResponse evolution(EvolutionRequest evolutionReq) throws BadRequestException {

		EvolutionResponse evolutionRes = new EvolutionResponse();
		evolutionResRse.execute(evolutionReq, evolutionRes, evolutionResearchService);
		return evolutionRes;
	}

	/**
	 * 進化コスト取得用API
	 *
	 * @param evoCostReq
	 * @return
	 * @throws BadRequestException
	 */
	@GetMapping("/evoCost")
	public EvoCostResponse evoCost(EvoCostRequest evoCostReq) throws BadRequestException {

		validationService.validation(evoCostReq);

		EvoCostResponse evoCostRes = new EvoCostResponse();
		Costs costs = Costs.valueOf(evoCostReq.getCosts());
		evoCostResearchService.exec(costs, evoCostRes);

		return evoCostRes;
	}

	/**
	 * 未実装ポケモン一覧取得用API
	 *
	 * @return
	 */
	@GetMapping("/unimplPokemon")
	public UnimplPokemonResponse unimplPokemon() {

		UnimplPokemonResponse res = new UnimplPokemonResponse();
		unimplPokemonService.exec(res);

		// 閲覧数を手動で追加。
		viewsCacheProvider.addTempList();

		return res;
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
