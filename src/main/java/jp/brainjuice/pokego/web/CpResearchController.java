package jp.brainjuice.pokego.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jp.brainjuice.pokego.business.service.ResearchServiceExecutor;
import jp.brainjuice.pokego.business.service.cp.AfterEvoCpResearchService;
import jp.brainjuice.pokego.business.service.cp.CpIvResearchService;
import jp.brainjuice.pokego.business.service.cp.CpRankListResearchService;
import jp.brainjuice.pokego.business.service.cp.CpRankResearchService;
import jp.brainjuice.pokego.business.service.cp.CpResearchService;
import jp.brainjuice.pokego.business.service.cp.ThreeGalarBirdsResearchService;
import jp.brainjuice.pokego.business.service.utils.ValidationService;
import jp.brainjuice.pokego.utils.exception.BadRequestException;
import jp.brainjuice.pokego.web.form.req.cp.AfterEvoCpRequest;
import jp.brainjuice.pokego.web.form.req.cp.CpIvRequest;
import jp.brainjuice.pokego.web.form.req.cp.CpRankListRequest;
import jp.brainjuice.pokego.web.form.req.cp.CpRankRequest;
import jp.brainjuice.pokego.web.form.req.cp.CpRequest;
import jp.brainjuice.pokego.web.form.req.cp.ThreeGalarBirdsRequest;
import jp.brainjuice.pokego.web.form.res.cp.AfterEvoCpResponse;
import jp.brainjuice.pokego.web.form.res.cp.CpIvResponse;
import jp.brainjuice.pokego.web.form.res.cp.CpRankListResponse;
import jp.brainjuice.pokego.web.form.res.cp.CpRankResponse;
import jp.brainjuice.pokego.web.form.res.cp.CpResponse;
import jp.brainjuice.pokego.web.form.res.cp.ThreeGalarBirdsResponse;
import lombok.extern.slf4j.Slf4j;

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

	private ValidationService validationService;

	@Autowired
	public CpResearchController(
			CpResearchService cpResearchService, ResearchServiceExecutor<CpResponse> cpResRse,
			CpRankResearchService cpRankResearchService, ResearchServiceExecutor<CpRankResponse> cpRankResRse,
			CpRankListResearchService cpRankListResearchService, ResearchServiceExecutor<CpRankListResponse> cpRankListResRse,
			AfterEvoCpResearchService afterEvoCpResearchService, ResearchServiceExecutor<AfterEvoCpResponse> afterEvoCpResRse,
			CpIvResearchService cpIvResearchService, ResearchServiceExecutor<CpIvResponse> cpIvResRse,
			ThreeGalarBirdsResearchService threeGalarBirdsResearchService, ResearchServiceExecutor<ThreeGalarBirdsResponse> threeGalarBirdsResRse,
			ValidationService validationService) {

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
		// 入力チェック
		this.validationService = validationService;
	}

	/**
	 * CP算出用API
	 *
	 * @param cpReq
	 * @return
	 * @throws Exception
	 */
	@GetMapping("/cp")
	public CpResponse cp(CpRequest cpReq) throws Exception {

		validationService.validation(cpReq);

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
	public CpRankResponse cpRank(CpRankRequest cpRankReq) throws Exception {

		validationService.validation(cpRankReq);

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

		validationService.validation(cpRankListReq);

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
	public AfterEvoCpResponse afterEvoCp(AfterEvoCpRequest afterEvoCpRequest) throws Exception {

		validationService.validation(afterEvoCpRequest);

		AfterEvoCpResponse afterEvoCpRes = new AfterEvoCpResponse();
		afterEvoCpResRse.execute(afterEvoCpRequest, afterEvoCpRes, afterEvoCpResearchService);
		return afterEvoCpRes;
	}

	/**
	 * ガラル三鳥の野生個体値を取得するAPIです。
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
	 * CPから個体値を算出するAPIです。
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
