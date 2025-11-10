package jp.brainjuice.pokego.web.search;


import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jp.brainjuice.pokego.business.service.search.pinnacle.GymRaidPinnacleRankService;
import jp.brainjuice.pokego.cache.service.ViewsCacheProvider;
import jp.brainjuice.pokego.utils.exception.BadRequestException;
import jp.brainjuice.pokego.web.search.form.req.pinnacle.GymRaidPinnacleRankRequest;
import jp.brainjuice.pokego.web.search.form.res.pinnacle.GymRaidPinnacleRankResponse;
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
public class PinnacleRankResearchController {

	private GymRaidPinnacleRankService gymRaidPinnacleRankService;
//	private ResearchServiceExecutor<EvolutionResponse> evolutionResRse;

	private ViewsCacheProvider viewsCacheProvider;

	public PinnacleRankResearchController (
			GymRaidPinnacleRankService gymRaidPinnacleRankService,
			ViewsCacheProvider viewsCacheProvider) {

		this.gymRaidPinnacleRankService = gymRaidPinnacleRankService;
		this.viewsCacheProvider = viewsCacheProvider;
	}

	/**
	 * 進化取得用API
	 *
	 * @param evolutionReq
	 * @return
	 * @throws BadRequestException
	 */
	@GetMapping("/gymRaidPinnacleRank")
	public GymRaidPinnacleRankResponse gymRaidPinnacleRank(@Valid GymRaidPinnacleRankRequest gymRaidPinnacleRankReq) throws BadRequestException {

		GymRaidPinnacleRankResponse gymRaidPinnacleRankRes = new GymRaidPinnacleRankResponse();
		gymRaidPinnacleRankService.exec(gymRaidPinnacleRankReq, gymRaidPinnacleRankRes);

		viewsCacheProvider.addTempList();
		return gymRaidPinnacleRankRes;
	}
}
