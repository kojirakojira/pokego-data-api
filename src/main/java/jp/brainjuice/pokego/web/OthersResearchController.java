package jp.brainjuice.pokego.web;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jp.brainjuice.pokego.business.service.research.ResearchServiceExecutor;
import jp.brainjuice.pokego.business.service.research.others.AbundanceResearchService;
import jp.brainjuice.pokego.business.service.research.others.EvolutionResearchService;
import jp.brainjuice.pokego.business.service.research.others.RaceResearchService;
import jp.brainjuice.pokego.cache.service.ViewsCacheProvider;
import jp.brainjuice.pokego.utils.exception.BadRequestException;
import jp.brainjuice.pokego.web.form.req.research.others.AbundanceRequest;
import jp.brainjuice.pokego.web.form.req.research.others.EvolutionRequest;
import jp.brainjuice.pokego.web.form.req.research.others.RaceRequest;
import jp.brainjuice.pokego.web.form.res.research.others.AbundanceResponse;
import jp.brainjuice.pokego.web.form.res.research.others.EvolutionResponse;
import jp.brainjuice.pokego.web.form.res.research.others.RaceResponse;

@RestController
@RequestMapping("/api")
public class OthersResearchController {

	private RaceResearchService raceResearchService;
	private ResearchServiceExecutor<RaceResponse> raceResRse;

	private EvolutionResearchService evolutionResearchService;
	private ResearchServiceExecutor<EvolutionResponse> evolutionResRse;

	private AbundanceResearchService abundanceResearchService;
	private ResearchServiceExecutor<AbundanceResponse> abundanceResRse;

	private ViewsCacheProvider viewsCacheProvider;

	public OthersResearchController (
			RaceResearchService raceResearchService, ResearchServiceExecutor<RaceResponse> raceResRse,
			EvolutionResearchService evolutionResearchService, ResearchServiceExecutor<EvolutionResponse> evolutionResRse,
			AbundanceResearchService abundanceResearchService, ResearchServiceExecutor<AbundanceResponse> abundanceResRse,
			ViewsCacheProvider viewsCacheProvider) {

		// 種族値検索
		this.raceResearchService = raceResearchService;
		this.raceResRse = raceResRse;

		// 進化ツリー
		this.evolutionResearchService = evolutionResearchService;
		this.evolutionResRse = evolutionResRse;

		// アバンダンス
		this.abundanceResearchService = abundanceResearchService;
		this.abundanceResRse = abundanceResRse;

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

}
