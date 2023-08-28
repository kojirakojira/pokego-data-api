package jp.brainjuice.pokego.web;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jp.brainjuice.pokego.business.service.ResearchServiceExecutor;
import jp.brainjuice.pokego.business.service.others.EvolutionResearchService;
import jp.brainjuice.pokego.business.service.others.UnimplPokemonService;
import jp.brainjuice.pokego.cache.service.ViewsCacheProvider;
import jp.brainjuice.pokego.utils.exception.BadRequestException;
import jp.brainjuice.pokego.web.form.req.others.EvolutionRequest;
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

	private UnimplPokemonService unimplPokemonService;

	private ViewsCacheProvider viewsCacheProvider;

	@Autowired
	public OthersResearchController (
			EvolutionResearchService evolutionResearchService, ResearchServiceExecutor<EvolutionResponse> evolutionResRse,
			UnimplPokemonService unimplPokemonService,
			ViewsCacheProvider viewsCacheProvider) {

		// 進化ツリー
		this.evolutionResearchService = evolutionResearchService;
		this.evolutionResRse = evolutionResRse;

		this.unimplPokemonService = unimplPokemonService;

		this.viewsCacheProvider = viewsCacheProvider;
	}

	/**
	 * 進化取得用API
	 *
	 * @param raceReq
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
		String errMsg = "不正なアクセスです。";
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
