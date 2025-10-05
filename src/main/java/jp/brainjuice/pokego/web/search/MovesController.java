package jp.brainjuice.pokego.web.search;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jp.brainjuice.pokego.business.service.search.ResearchServiceExecutor;
import jp.brainjuice.pokego.business.service.search.moves.MoveListService;
import jp.brainjuice.pokego.business.service.search.moves.MoveLookupService;
import jp.brainjuice.pokego.business.service.search.moves.PokemonAttackResearchService;
import jp.brainjuice.pokego.cache.service.ViewsCacheProvider;
import jp.brainjuice.pokego.utils.exception.BadRequestException;
import jp.brainjuice.pokego.web.search.form.req.moves.MoveLookupRequest;
import jp.brainjuice.pokego.web.search.form.req.moves.PokemonAttackRequest;
import jp.brainjuice.pokego.web.search.form.res.moves.MoveListResponse;
import jp.brainjuice.pokego.web.search.form.res.moves.MoveLookupResponse;
import jp.brainjuice.pokego.web.search.form.res.moves.PokemonAttackResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * 技の情報を取得するコントローラクラス
 *
 * @author saibabanagchampa
 *
 */
@RestController
@RequestMapping("/api")
@Slf4j
public class MovesController {

	private MoveListService moveListService;

	private PokemonAttackResearchService pokemonAttackResearchService;
	private ResearchServiceExecutor<PokemonAttackResponse> pokemonAttackResRse;

	private MoveLookupService moveLookupService;

	private ViewsCacheProvider viewsCacheProvider;

	public MovesController(
			MoveListService moveListService,
			PokemonAttackResearchService pokemonAttackResearchService, ResearchServiceExecutor<PokemonAttackResponse> pokemonAttackResRse,
			MoveLookupService moveLookupService,
			ViewsCacheProvider viewsCacheProvider) {
		this.moveListService = moveListService;
		this.pokemonAttackResearchService = pokemonAttackResearchService;
		this.pokemonAttackResRse = pokemonAttackResRse;
		this.moveLookupService = moveLookupService;
		this.viewsCacheProvider = viewsCacheProvider;
	}

	@GetMapping("/moveList")
	public MoveListResponse moveList() {

		MoveListResponse res = new MoveListResponse();

		moveListService.exec(res);

		viewsCacheProvider.addTempList();

		return res;
	}

	/**
	 * そのポケモンが覚える通常技を取得する
	 *
	 * @param req
	 * @return
	 * @throws BadRequestException
	 */
	@GetMapping("/pokemonAttack")
	public PokemonAttackResponse pokemonAttack(PokemonAttackRequest req) throws BadRequestException {

		PokemonAttackResponse res = new PokemonAttackResponse();
		pokemonAttackResRse.execute(req, res, pokemonAttackResearchService);

		viewsCacheProvider.addTempList();
		return res;
	}

	@GetMapping("/moveLookup")
	public MoveLookupResponse moveLookup(MoveLookupRequest req) throws BadRequestException {

		MoveLookupResponse res = new MoveLookupResponse();
		moveLookupService.check(req, res);

		moveLookupService.execute(req, res);

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
