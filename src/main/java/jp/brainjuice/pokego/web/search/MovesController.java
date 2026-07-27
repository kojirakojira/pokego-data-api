package jp.brainjuice.pokego.web.search;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jp.brainjuice.pokego.business.service.search.ResearchServiceExecutor;
import jp.brainjuice.pokego.business.service.search.moves.FilterAllMoveService;
import jp.brainjuice.pokego.business.service.search.moves.GymRaidPokeMoveCombiResearchService;
import jp.brainjuice.pokego.business.service.search.moves.MoveLookupService;
import jp.brainjuice.pokego.business.service.search.moves.PokemonAttackResearchService;
import jp.brainjuice.pokego.cache.service.ViewsCacheProvider;
import jp.brainjuice.pokego.utils.exception.BadRequestException;
import jp.brainjuice.pokego.web.search.form.req.moves.FilterAllMoveRequest;
import jp.brainjuice.pokego.web.search.form.req.moves.GymRaidPokeMoveCombiRequest;
import jp.brainjuice.pokego.web.search.form.req.moves.MoveLookupRequest;
import jp.brainjuice.pokego.web.search.form.req.moves.PokemonAttackRequest;
import jp.brainjuice.pokego.web.search.form.res.moves.FilterAllMoveResponse;
import jp.brainjuice.pokego.web.search.form.res.moves.GymRaidPokeMoveCombiResponse;
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

	private FilterAllMoveService filterAllMoveService;

	private PokemonAttackResearchService pokemonAttackResearchService;
	private ResearchServiceExecutor<PokemonAttackResponse> pokemonAttackResRse;

	private GymRaidPokeMoveCombiResearchService gymRaidPokeMoveCombiResearchService;
	private ResearchServiceExecutor<GymRaidPokeMoveCombiResponse> gymRaidPokeMoveCombiResRse;

	private MoveLookupService moveLookupService;

	private ViewsCacheProvider viewsCacheProvider;

	public MovesController(
			FilterAllMoveService filterAllMoveService,
			PokemonAttackResearchService pokemonAttackResearchService, ResearchServiceExecutor<PokemonAttackResponse> pokemonAttackResRse,
			GymRaidPokeMoveCombiResearchService gymRaidPokeMoveCombiResearchService, ResearchServiceExecutor<GymRaidPokeMoveCombiResponse> gymRaidPokeMoveCombiResRse,
			MoveLookupService moveLookupService,
			ViewsCacheProvider viewsCacheProvider) {
		this.filterAllMoveService = filterAllMoveService;
		this.pokemonAttackResearchService = pokemonAttackResearchService;
		this.pokemonAttackResRse = pokemonAttackResRse;
		this.gymRaidPokeMoveCombiResearchService = gymRaidPokeMoveCombiResearchService;
		this.gymRaidPokeMoveCombiResRse = gymRaidPokeMoveCombiResRse;
		this.moveLookupService = moveLookupService;
		this.viewsCacheProvider = viewsCacheProvider;
	}

	/**
	 * 全技絞り込み
	 *
	 * @param req
	 * @return
	 * @throws BadRequestException
	 */
	@GetMapping("/filterAllMove")
	public FilterAllMoveResponse filterAllMove(FilterAllMoveRequest req) {

		FilterAllMoveResponse res = new FilterAllMoveResponse();

		filterAllMoveService.exec(req, res);

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

		return res;
	}

	@GetMapping("gymRaidPokeMoveCombi")
	public GymRaidPokeMoveCombiResponse pokemonMoveCombi(GymRaidPokeMoveCombiRequest req) throws BadRequestException {

		GymRaidPokeMoveCombiResponse res = new GymRaidPokeMoveCombiResponse();
		gymRaidPokeMoveCombiResRse.execute(req, res, gymRaidPokeMoveCombiResearchService);

		return res;
	}

	/**
	 * 技の情報を取得する。
	 *
	 * @param req
	 * @return
	 * @throws BadRequestException
	 */
	@GetMapping("/moveLookup")
	public MoveLookupResponse moveLookup(MoveLookupRequest req) throws BadRequestException {

		MoveLookupResponse res = new MoveLookupResponse();
		if (!moveLookupService.check(req, res)) {
			return res;
		}

		moveLookupService.execute(req, res);

		viewsCacheProvider.addTempList();

		return res;
	}
}
