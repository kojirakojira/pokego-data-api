package jp.brainjuice.pokego.web.search;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jp.brainjuice.pokego.business.service.search.ResearchServiceExecutor;
import jp.brainjuice.pokego.business.service.search.cp.AfterEvoCpResearchService;
import jp.brainjuice.pokego.business.service.search.cp.CpIvResearchService;
import jp.brainjuice.pokego.business.service.search.cp.CpRankListResearchService;
import jp.brainjuice.pokego.business.service.search.cp.CpRankResearchService;
import jp.brainjuice.pokego.business.service.search.cp.CpResearchService;
import jp.brainjuice.pokego.business.service.search.cp.ThreeGalarBirdsResearchService;
import jp.brainjuice.pokego.dao.jpa.entity.GoPokedex;
import jp.brainjuice.pokego.web.search.form.req.cp.AfterEvoCpRequest;
import jp.brainjuice.pokego.web.search.form.req.cp.CpIvRequest;
import jp.brainjuice.pokego.web.search.form.req.cp.CpRankListRequest;
import jp.brainjuice.pokego.web.search.form.req.cp.CpRankRequest;
import jp.brainjuice.pokego.web.search.form.req.cp.CpRequest;
import jp.brainjuice.pokego.web.search.form.req.cp.ThreeGalarBirdsRequest;
import jp.brainjuice.pokego.web.search.form.res.cp.AfterEvoCpResponse;
import jp.brainjuice.pokego.web.search.form.res.cp.CpIvResponse;
import jp.brainjuice.pokego.web.search.form.res.cp.CpRankListResponse;
import jp.brainjuice.pokego.web.search.form.res.cp.CpRankResponse;
import jp.brainjuice.pokego.web.search.form.res.cp.CpResponse;
import jp.brainjuice.pokego.web.search.form.res.cp.ThreeGalarBirdsResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * CPを算出したり、個体値の一覧を取得したりするためのコントローラクラス
 *
 * @author saibabanagchampa
 *
 */
@RestController
@RequestMapping("/api")
@Slf4j
public class CpResearchController {

	private CpResearchService cpResearchService;
	private ResearchServiceExecutor<CpResponse> cpResRse;

	private CpRankResearchService cpRankResearchService;
	private ResearchServiceExecutor<CpRankResponse> cpRankResRse;

	private CpRankListResearchService cpRankListResearchService;
	private ResearchServiceExecutor<CpRankListResponse> cpRankListResRse;

	private AfterEvoCpResearchService afterEvoCpResearchService;
	private ResearchServiceExecutor<AfterEvoCpResponse> afterEvoCpResRse;

	private CpIvResearchService cpIvResearchService;
	private ResearchServiceExecutor<CpIvResponse> cpIvResRse;

	private ThreeGalarBirdsResearchService threeGalarBirdsResearchService;
	private ResearchServiceExecutor<ThreeGalarBirdsResponse> threeGalarBirdsResRse;

	public CpResearchController(
			CpResearchService cpResearchService, ResearchServiceExecutor<CpResponse> cpResRse,
			CpRankResearchService cpRankResearchService, ResearchServiceExecutor<CpRankResponse> cpRankResRse,
			CpRankListResearchService cpRankListResearchService, ResearchServiceExecutor<CpRankListResponse> cpRankListResRse,
			AfterEvoCpResearchService afterEvoCpResearchService, ResearchServiceExecutor<AfterEvoCpResponse> afterEvoCpResRse,
			CpIvResearchService cpIvResearchService, ResearchServiceExecutor<CpIvResponse> cpIvResRse,
			ThreeGalarBirdsResearchService threeGalarBirdsResearchService, ResearchServiceExecutor<ThreeGalarBirdsResponse> threeGalarBirdsResRse) {

		// CP算出
		this.cpResearchService = cpResearchService;
		this.cpResRse = cpResRse;
		// CP順位算出
		this.cpRankResearchService = cpRankResearchService;
		this.cpRankResRse = cpRankResRse;
		// CP順位一覧取得
		this.cpRankListResearchService = cpRankListResearchService;
		this.cpRankListResRse = cpRankListResRse;
		// 進化後CP
		this.afterEvoCpResearchService = afterEvoCpResearchService;
		this.afterEvoCpResRse = afterEvoCpResRse;
		// 野生個体値
		this.cpIvResearchService = cpIvResearchService;
		this.cpIvResRse = cpIvResRse;
		// 野生個体値
		this.threeGalarBirdsResearchService = threeGalarBirdsResearchService;
		this.threeGalarBirdsResRse = threeGalarBirdsResRse;
	}

	/**
	 * CP算出用API
	 *
	 * @param cpReq
	 * @return
	 * @throws Exception
	 */
	@GetMapping("/cp")
	public CpResponse cp(@Valid CpRequest cpReq) throws Exception {

		CpResponse cpRes = new CpResponse();

		if (!cpResearchService.check(cpReq.getPl(), cpRes)) {
			return cpRes;
		}

		cpResRse.execute(cpReq, cpRes, cpResearchService);

		return cpRes;
	}

	/**
	 * CPランキング算出用API
	 *
	 * @param cpRankReq
	 * @return
	 * @throws Exception
	 */
	@GetMapping("/cpRank")
	public CpRankResponse cpRank(@Valid CpRankRequest cpRankReq) throws Exception {

		CpRankResponse cpRankRes = new CpRankResponse();
		cpRankResRse.execute(cpRankReq, cpRankRes, cpRankResearchService);

		return cpRankRes;
	}

	/**
	 * CPランキング一覧取得用API
	 *
	 * @param cpRankListReq
	 * @return
	 * @throws Exception
	 */
	@GetMapping("/cpRankList")
	public CpRankListResponse cpRankList(CpRankListRequest cpRankListReq) throws Exception {

		CpRankListResponse cpRankListRes = new CpRankListResponse();
		cpRankListResRse.execute(cpRankListReq, cpRankListRes, cpRankListResearchService);
		return cpRankListRes;
	}

	/**
	 * 進化後CP取得用API
	 *
	 * @param afterEvoCpRequest
	 * @return
	 * @throws Exception
	 */
	@GetMapping("/afterEvoCp")
	public AfterEvoCpResponse afterEvoCp(@Valid AfterEvoCpRequest afterEvoCpRequest) throws Exception {

		AfterEvoCpResponse afterEvoCpRes = new AfterEvoCpResponse();
		afterEvoCpResRse.execute(afterEvoCpRequest, afterEvoCpRes, afterEvoCpResearchService);
		return afterEvoCpRes;
	}

	/**
	 * ガラル三鳥のGoPokedexのリスト取得するAPI
	 *
	 * @param raidReq
	 * @param req
	 * @return
	 * @throws Exception
	 */
	@GetMapping("/threeGalarBirdList")
	public ResponseEntity<List<GoPokedex>> threeGalarBirdList() throws Exception {
		// ResponseEntityを使用してもこの場合は変わらないが、備忘的な意味でこの書き方で実装。
		// 返却値は以下のような形式になる。
		// { 0: { GoPokedexの内容 }, 1: { GoPokedexの内容 }, ...}
		List<GoPokedex> tgbGpList = threeGalarBirdsResearchService.getList();
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		return new ResponseEntity<>(tgbGpList, headers, HttpStatus.OK);
	}

	/**
	 * ガラル三鳥の野生個体値を取得するAPI
	 *
	 * @param raidReq
	 * @param req
	 * @return
	 * @throws Exception
	 */
	@GetMapping("/threeGalarBirds")
	public ThreeGalarBirdsResponse threeGalarBirdsIv(ThreeGalarBirdsRequest tgbReq) throws Exception {

		ThreeGalarBirdsResponse tgbRes = new ThreeGalarBirdsResponse();
		threeGalarBirdsResRse.execute(tgbReq, tgbRes, threeGalarBirdsResearchService);
		return tgbRes;
	}

	/**
	 * CPから個体値を算出するAPI
	 *
	 * @param cpIvReq
	 * @param req
	 * @return
	 * @throws Exception
	 */
	@GetMapping("/cpIv")
	public CpIvResponse cpIv(CpIvRequest cpIvReq) throws Exception {

		CpIvResponse cpIvRes = new CpIvResponse();
		cpIvResRse.execute(cpIvReq, cpIvRes, cpIvResearchService);
		return cpIvRes;
	}
}
