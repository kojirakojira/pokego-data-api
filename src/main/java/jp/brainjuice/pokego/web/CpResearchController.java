package jp.brainjuice.pokego.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jp.brainjuice.pokego.business.service.research.ResearchServiceExecutor;
import jp.brainjuice.pokego.business.service.research.cp.CpRankListResearchService;
import jp.brainjuice.pokego.business.service.research.cp.CpRankResearchService;
import jp.brainjuice.pokego.business.service.research.cp.CpResearchService;
import jp.brainjuice.pokego.business.service.research.cp.FRTaskResearchService;
import jp.brainjuice.pokego.business.service.research.cp.RaidResearchService;
import jp.brainjuice.pokego.business.service.research.cp.ShadowResearchService;
import jp.brainjuice.pokego.business.service.utils.InputCheckService;
import jp.brainjuice.pokego.utils.exception.BadRequestException;
import jp.brainjuice.pokego.web.form.req.research.cp.CpRankListRequest;
import jp.brainjuice.pokego.web.form.req.research.cp.CpRankRequest;
import jp.brainjuice.pokego.web.form.req.research.cp.CpRequest;
import jp.brainjuice.pokego.web.form.req.research.cp.FRTaskRequest;
import jp.brainjuice.pokego.web.form.req.research.cp.RaidRequest;
import jp.brainjuice.pokego.web.form.req.research.cp.ShadowRequest;
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
			RaidResearchService raidResearchService, ResearchServiceExecutor<RaidResponse> raidResRse,
			FRTaskResearchService fRTaskResearchService, ResearchServiceExecutor<FRTaskResponse> fRTaskResRse,
			ShadowResearchService shadowResearchService, ResearchServiceExecutor<ShadowResponse> shadowResRse,
			InputCheckService inputCheckService) {
		// CP算出
		this.cpResearchService = cpResearchService;
		this.cpResRse = cpResRse;
		// CPランク算出
		this.cpRankResearchService = cpRankResearchService;
		this.cpRankResRse = cpRankResRse;
		// CPランク一覧取得
		this.cpRankListResearchService = cpRankListResearchService;
		this.cpRankListResRse = cpRankListResRse;
		// レイドボスCP算出
		this.raidResearchService = raidResearchService;
		this.raidResRse = raidResRse;
		// フィールドリサーチCP算出
		this.fRTaskResearchService = fRTaskResearchService;
		this.fRTaskResRse = fRTaskResRse;
		// シャドウCP算出
		this.shadowResearchService = shadowResearchService;
		this.shadowResRse = shadowResRse;
		// 入力チェック
		this.inputCheckService = inputCheckService;
	}

	/**
	 * CPを求めるAPIです。<br>
	 * INPUT：
	 * <ul>
	 *   <li>id or name</li>
	 *   <li>iva</li>
	 *   <li>ivd</li>
	 *   <li>ivh</li>
	 *   <li>pl</li>
	 * </ul>
	 *
	 * @param cpReq
	 * @return
	 * @throws Exception
	 */
	@GetMapping("/cp")
	public CpResponse cp(CpRequest cpReq) throws Exception {

		inputCheckService.validation(cpReq);

		CpResponse cpRes = new CpResponse();
		cpResRse.execute(cpReq, cpRes, cpResearchService);

		return cpRes;
	}

	/**
	 * CPの順位を求めるAPIです。<br>
	 * INPUT：
	 * <ul>
	 *   <li>id or name</li>
	 *   <li>iva</li>
	 *   <li>ivd</li>
	 *   <li>ivh</li>
	 * </ul>
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
	 * 対象のポケモンのCPの一覧をランキングで取得するAPIです。<br>
	 * ※PLが40の場合のCPの一覧です。
	 * INPUT：
	 * <ul>
	 *   <li>id or name</li>
	 *   <li>iva</li>
	 *   <li>ivd</li>
	 *   <li>ivh</li>
	 * </ul>
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
