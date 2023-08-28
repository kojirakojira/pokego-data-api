package jp.brainjuice.pokego.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jp.brainjuice.pokego.business.service.ResearchServiceExecutor;
import jp.brainjuice.pokego.business.service.catchCp.FRTaskResearchService;
import jp.brainjuice.pokego.business.service.catchCp.RaidResearchService;
import jp.brainjuice.pokego.business.service.catchCp.RocketResearchService;
import jp.brainjuice.pokego.business.service.utils.ValidationService;
import jp.brainjuice.pokego.utils.exception.BadRequestException;
import jp.brainjuice.pokego.web.form.req.catchCp.FRTaskRequest;
import jp.brainjuice.pokego.web.form.req.catchCp.RaidRequest;
import jp.brainjuice.pokego.web.form.req.catchCp.RocketRequest;
import jp.brainjuice.pokego.web.form.res.catchCp.FRTaskResponse;
import jp.brainjuice.pokego.web.form.res.catchCp.RaidResponse;
import jp.brainjuice.pokego.web.form.res.catchCp.RocketResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * 捕獲時CPを算出するコントローラクラス
 *
 * @author saibabanagchampa
 *
 */
@RestController
@RequestMapping("/api")
@Slf4j
public class CatchCpResearchController {

	private RaidResearchService raidResearchService;
	private ResearchServiceExecutor<RaidResponse> raidResRse;

	private FRTaskResearchService fRTaskResearchService;
	private ResearchServiceExecutor<FRTaskResponse> fRTaskResRse;

	private RocketResearchService rocketResearchService;
	private ResearchServiceExecutor<RocketResponse> rocketResRse;

	private ValidationService validationService;

	@Autowired
	public CatchCpResearchController(
			RaidResearchService raidResearchService, ResearchServiceExecutor<RaidResponse> raidResRse,
			FRTaskResearchService fRTaskResearchService, ResearchServiceExecutor<FRTaskResponse> fRTaskResRse,
			RocketResearchService rocketResearchService, ResearchServiceExecutor<RocketResponse> rocketResRse,
			ValidationService validationService) {

		// レイドボスCP算出
		this.raidResearchService = raidResearchService;
		this.raidResRse = raidResRse;
		// フィールドリサーチ、タマゴCP算出
		this.fRTaskResearchService = fRTaskResearchService;
		this.fRTaskResRse = fRTaskResRse;
		// シャドウCP算出
		this.rocketResearchService = rocketResearchService;
		this.rocketResRse = rocketResRse;
		// 入力チェック
		this.validationService = validationService;
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

		validationService.validation(raidReq);

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

		validationService.validation(fRTaskReq);

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

		validationService.validation(fRTaskReq);

		FRTaskResponse fRTaskRes = new FRTaskResponse();
		fRTaskResRse.execute(fRTaskReq, fRTaskRes, fRTaskResearchService);
		return fRTaskRes;
	}

	/**
	 * ロケット団勝利ボーナスで獲得できるポケモンのCP最高値・最低値を求めるAPIです。
	 *
	 * @param raidReq
	 * @param req
	 * @return
	 * @throws Exception
	 */
	@GetMapping("/rocket")
	public RocketResponse shadow(RocketRequest rocketReq) throws Exception {

		validationService.validation(rocketReq);

		RocketResponse shadowRes = new RocketResponse();
		rocketResRse.execute(rocketReq, shadowRes, rocketResearchService);
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
