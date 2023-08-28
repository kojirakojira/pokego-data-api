package jp.brainjuice.pokego.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jp.brainjuice.pokego.business.service.ResearchServiceExecutor;
import jp.brainjuice.pokego.business.service.general.AbundanceResearchService;
import jp.brainjuice.pokego.business.service.general.PokemonSearchService;
import jp.brainjuice.pokego.business.service.race.RaceDiffService;
import jp.brainjuice.pokego.business.service.utils.dto.PokemonFilterResult;
import jp.brainjuice.pokego.business.service.utils.dto.PokemonSearchResult;
import jp.brainjuice.pokego.cache.service.ViewsCacheProvider;
import jp.brainjuice.pokego.utils.exception.BadRequestException;
import jp.brainjuice.pokego.web.form.req.general.AbundanceRequest;
import jp.brainjuice.pokego.web.form.req.general.FilterAllRequest;
import jp.brainjuice.pokego.web.form.req.general.SearchAllRequest;
import jp.brainjuice.pokego.web.form.res.general.AbundanceResponse;
import jp.brainjuice.pokego.web.form.res.general.FilterAllResponse;
import jp.brainjuice.pokego.web.form.res.general.SearchAllResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * 一般機能のコントローラクラス
 *
 * @author saibabanagchampa
 *
 */
@RestController
@RequestMapping("/api")
@Slf4j
public class GeneralController {

	private AbundanceResearchService abundanceResearchService;
	private ResearchServiceExecutor<AbundanceResponse> abundanceResRse;

	private PokemonSearchService pokemonSearchService;

	private ViewsCacheProvider viewsCacheProvider;

	@Autowired
	public GeneralController(
			AbundanceResearchService abundanceResearchService, ResearchServiceExecutor<AbundanceResponse> abundanceResRse,
			RaceDiffService raceDiffService,
			PokemonSearchService pokemonSearchService,
			ViewsCacheProvider viewsCacheProvider) {

		// アバンダンス
		this.abundanceResearchService = abundanceResearchService;
		this.abundanceResRse = abundanceResRse;

		// 検索
		this.pokemonSearchService = pokemonSearchService;

		this.viewsCacheProvider = viewsCacheProvider;
	}

	/**
	 * アバンダンス取得用API
	 *
	 * @param abundanceReq
	 * @return
	 * @throws BadRequestException
	 */
	@GetMapping("/abundance")
	public AbundanceResponse abundance(AbundanceRequest abundanceReq) throws BadRequestException {

		AbundanceResponse abundanceRes = new AbundanceResponse();
		abundanceResRse.execute(abundanceReq, abundanceRes, abundanceResearchService);
		return abundanceRes;
	}

	/**
	 * ポケモン検索用API
	 *
	 * @param name
	 * @return
	 */
	@GetMapping("/searchAll")
	public SearchAllResponse search(SearchAllRequest req) {

		SearchAllResponse res = new SearchAllResponse();
		PokemonSearchResult psr = pokemonSearchService.search(req.getName());
		res.setSuccess(true);
		res.setMessage("");
		res.setPokemonSearchResult(psr);

		// 閲覧数を手動で追加。
		viewsCacheProvider.addTempList();

		return res;
	}

	/**
	 * 絞り込み検索用API
	 *
	 * @param req
	 * @return
	 */
	@GetMapping("/filterAll")
	public FilterAllResponse filterAll(FilterAllRequest req) {

		FilterAllResponse res = new FilterAllResponse();
		PokemonFilterResult pfr = pokemonSearchService.filter(req);
		res.setPfr(pfr);
		res.setSuccess(true);
		res.setMessage("");

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
