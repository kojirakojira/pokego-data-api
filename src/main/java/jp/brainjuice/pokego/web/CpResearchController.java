package jp.brainjuice.pokego.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jp.brainjuice.pokego.business.service.research.ResearchServiceExecutor;
import jp.brainjuice.pokego.business.service.research.cp.AfterEvoCpResearchService;
import jp.brainjuice.pokego.business.service.research.cp.CpRankListResearchService;
import jp.brainjuice.pokego.business.service.research.cp.CpRankResearchService;
import jp.brainjuice.pokego.business.service.research.cp.CpResearchService;
import jp.brainjuice.pokego.business.service.research.cp.FRTaskResearchService;
import jp.brainjuice.pokego.business.service.research.cp.RaidResearchService;
import jp.brainjuice.pokego.business.service.research.cp.ShadowResearchService;
import jp.brainjuice.pokego.business.service.utils.InputCheckService;
import jp.brainjuice.pokego.utils.exception.BadRequestException;
import jp.brainjuice.pokego.web.form.req.research.cp.AfterEvoCpRequest;
import jp.brainjuice.pokego.web.form.req.research.cp.CpRankListRequest;
import jp.brainjuice.pokego.web.form.req.research.cp.CpRankRequest;
import jp.brainjuice.pokego.web.form.req.research.cp.CpRequest;
import jp.brainjuice.pokego.web.form.req.research.cp.FRTaskRequest;
import jp.brainjuice.pokego.web.form.req.research.cp.RaidRequest;
import jp.brainjuice.pokego.web.form.req.research.cp.ShadowRequest;
import jp.brainjuice.pokego.web.form.res.research.cp.AfterEvoCpResponse;
import jp.brainjuice.pokego.web.form.res.research.cp.CpRankListResponse;
import jp.brainjuice.pokego.web.form.res.research.cp.CpRankResponse;
import jp.brainjuice.pokego.web.form.res.research.cp.CpResponse;
import jp.brainjuice.pokego.web.form.res.research.cp.FRTaskResponse;
import jp.brainjuice.pokego.web.form.res.research.cp.RaidResponse;
import jp.brainjuice.pokego.web.form.res.research.cp.ShadowResponse;
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

	private RaidResearchService raidResearchService;
	private ResearchServiceExecutor<RaidResponse> raidResRse;

	private FRTaskResearchService fRTaskResearchService;
	private ResearchServiceExecutor<FRTaskResponse> fRTaskResRse;

	private ShadowResearchService shadowResearchService;
	private ResearchServiceExecutor<ShadowResponse> shadowResRse;

	private InputCheckService inputCheckService;

	@Autowired
	public CpResearchController(
			CpResearchService cpResearchService, ResearchServiceExecutor<CpResponse> cpResRse,
			CpRankResearchService cpRankResearchService, ResearchServiceExecutor<CpRankResponse> cpRankResRse,
			CpRankListResearchService cpRankListResearchService, ResearchServiceExecutor<CpRankListResponse> cpRankListResRse,
			AfterEvoCpResearchService afterEvoCpResearchService, ResearchServiceExecutor<AfterEvoCpResponse> afterEvoCpResRse,
			RaidResearchService raidResearchService, ResearchServiceExecutor<RaidResponse> raidResRse,
			FRTaskResearchService fRTaskResearchService, ResearchServiceExecutor<FRTaskResponse> fRTaskResRse,
			ShadowResearchService shadowResearchService, ResearchServiceExecutor<ShadowResponse> shadowResRse,
			InputCheckService inputCheckService) {

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
		// レイドボスCP算出
		this.raidResearchService = raidResearchService;
		this.raidResRse = raidResRse;
		// フィールドリサーチ、タマゴCP算出
		this.fRTaskResearchService = fRTaskResearchService;
		this.fRTaskResRse = fRTaskResRse;
		// シャドウCP算出
		this.shadowResearchService = shadowResearchService;
		this.shadowResRse = shadowResRse;
		// 入力チェック
		this.inputCheckService = inputCheckService;
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

		inputCheckService.validation(cpReq);

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

		inputCheckService.validation(cpRankReq);

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

		inputCheckService.validation(cpRankListReq);

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

		inputCheckService.validation(afterEvoCpRequest);

		AfterEvoCpResponse afterEvoCpRes = new AfterEvoCpResponse();
		afterEvoCpResRse.execute(afterEvoCpRequest, afterEvoCpRes, afterEvoCpResearchService);
		return afterEvoCpRes;
	}

	/**
	 * レイドボスのCP最高値・最低値を求めるAPIです。
	 *
	 * @param raidReq
	 * @return
	 * @throws Exception
	 */
	@GetMapping("/raid")
	public RaidResponse raid(RaidRequest raidReq) throws Exception {

		inputCheckService.validation(raidReq);

		RaidResponse raidRes = new RaidResponse();
		raidResRse.execute(raidReq, raidRes, raidResearchService);
		return raidRes;
	}

	/**
	 * フィールドリサーチタスクのCP最高値・最低値を求めるAPIです。
	 *
	 * @param raidReq
	 * @param req
	 * @return
	 * @throws Exception
	 */
	@GetMapping("/fRTask")
	public FRTaskResponse fRTask(FRTaskRequest fRTaskReq) throws Exception {

		inputCheckService.validation(fRTaskReq);

		FRTaskResponse fRTaskRes = new FRTaskResponse();
		fRTaskResRse.execute(fRTaskReq, fRTaskRes, fRTaskResearchService);
		return fRTaskRes;
	}

	/**
	 * タマゴCP最高値・最低値を求めるAPIです。<br>
	 * フィールドリサーチと全く同じなので、処理を使いまわします。
	 *
	 * @param raidReq
	 * @param req
	 * @return
	 * @throws Exception
	 */
	@GetMapping("/eggs")
	public FRTaskResponse eggs(FRTaskRequest fRTaskReq) throws Exception {

		inputCheckService.validation(fRTaskReq);

		FRTaskResponse fRTaskRes = new FRTaskResponse();
		fRTaskResRse.execute(fRTaskReq, fRTaskRes, fRTaskResearchService);
		return fRTaskRes;
	}

	/**
	 * シャドウのCP最高値・最低値を求めるAPIです。
	 *
	 * @param raidReq
	 * @param req
	 * @return
	 * @throws Exception
	 */
	@GetMapping("/shadow")
	public ShadowResponse shadow(ShadowRequest shadowReq) throws Exception {

		inputCheckService.validation(shadowReq);

		ShadowResponse shadowRes = new ShadowResponse();
		shadowResRse.execute(shadowReq, shadowRes, shadowResearchService);
		return shadowRes;
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
