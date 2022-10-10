package jp.brainjuice.pokego.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jp.brainjuice.pokego.business.service.GoConvertService;
import jp.brainjuice.pokego.business.service.PokemonSearchService;
import jp.brainjuice.pokego.business.service.research.PlResearchService;
import jp.brainjuice.pokego.business.service.research.ResearchServiceExecutor;
import jp.brainjuice.pokego.business.service.utils.dto.PokemonSearchResult;
import jp.brainjuice.pokego.cache.dao.PageViewRedisRepository;
import jp.brainjuice.pokego.cache.dao.entity.PageViewInfo;
import jp.brainjuice.pokego.utils.exception.BadRequestException;
import jp.brainjuice.pokego.web.form.req.research.PlRequest;
import jp.brainjuice.pokego.web.form.res.research.PlResponse;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api")
@Slf4j
public class SearchController {

	private PlResearchService plResearchService;
	private ResearchServiceExecutor<PlResponse> plResRse;

	private GoConvertService goConvertService;

	private PokemonSearchService pokemonSearchService;

	@Autowired
	PageViewRedisRepository pageViewInfoRepository;

	@Autowired
	public SearchController(
			PlResearchService plResearchService, ResearchServiceExecutor<PlResponse> plResRse,
			GoConvertService goConvertService,
			PokemonSearchService pokemonSearchService) {
		// PL算出
		this.plResearchService = plResearchService;
		this.plResRse = plResRse;

		this.goConvertService = goConvertService;
		// 検索
		this.pokemonSearchService = pokemonSearchService;
	}

	@GetMapping("/go")
	public String go(String id) {

		return goConvertService.getGoStatus(id);
	}

	@GetMapping("/save")
	public String save() {

		return goConvertService.insertGoPokedexAll();
	}

	@GetMapping("/search")
	public PokemonSearchResult search(String name) {
		return pokemonSearchService.search(name);
	}

	@GetMapping("/pl")
	public PlResponse pl(@Validated PlRequest plReq) throws BadRequestException {

		PlResponse plRes = new PlResponse();
		plResRse.execute(plReq, plRes, plResearchService);
		return plRes;
	}

	@GetMapping("/test")
	public String test() {

		PageViewInfo pvi = new PageViewInfo("page", "ip", null);
		pageViewInfoRepository.save(pvi);

		return pageViewInfoRepository.findAll().toString();
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
