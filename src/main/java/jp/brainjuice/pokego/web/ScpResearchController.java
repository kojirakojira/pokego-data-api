package jp.brainjuice.pokego.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jp.brainjuice.pokego.business.service.ResearchServiceExecutor;
import jp.brainjuice.pokego.business.service.scp.AfterEvoScpRankResearchService;
import jp.brainjuice.pokego.business.service.scp.ScpRankListResearchService;
import jp.brainjuice.pokego.business.service.scp.ScpRankMaxMinResearchService;
import jp.brainjuice.pokego.business.service.scp.ScpRankResearchService;
import jp.brainjuice.pokego.business.service.utils.ValidationService;
import jp.brainjuice.pokego.utils.exception.BadRequestException;
import jp.brainjuice.pokego.web.form.req.scp.AfterEvoScpRankRequest;
import jp.brainjuice.pokego.web.form.req.scp.ScpRankListRequest;
import jp.brainjuice.pokego.web.form.req.scp.ScpRankMaxMinRequest;
import jp.brainjuice.pokego.web.form.req.scp.ScpRankRequest;
import jp.brainjuice.pokego.web.form.res.scp.AfterEvoScpRankResponse;
import jp.brainjuice.pokego.web.form.res.scp.ScpRankListResponse;
import jp.brainjuice.pokego.web.form.res.scp.ScpRankMaxMinResponse;
import jp.brainjuice.pokego.web.form.res.scp.ScpRankResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * PvP順位(SCP)を算出したり、一覧を取得したりするためのコントローラクラス
 *
 * @author saibabanagchampa
 *
 */
@RestController
@RequestMapping("/api")
@Slf4j
public class ScpResearchController {

	private ScpRankResearchService scpRankResearchService;
	private ResearchServiceExecutor<ScpRankResponse> scpRankResRse;

	private ScpRankMaxMinResearchService scpRankMaxMinResearchService;
	private ResearchServiceExecutor<ScpRankMaxMinResponse> scpRankMaxMinResRse;

	private ScpRankListResearchService scpRankListResearchService;
	private ResearchServiceExecutor<ScpRankListResponse> scpRankListResRse;

	private AfterEvoScpRankResearchService afterEvoScpRankResearchService;
	private ResearchServiceExecutor<AfterEvoScpRankResponse> afterEvoScpRankResRse;

	private ValidationService validationService;

	@Autowired
	public ScpResearchController(
			ScpRankResearchService scpRankResearchService, ResearchServiceExecutor<ScpRankResponse> scpRankResRse,
			ScpRankMaxMinResearchService scpRankMaxMinResearchService, ResearchServiceExecutor<ScpRankMaxMinResponse> scpRankMaxMinResRse,
			ScpRankListResearchService scpRankListResearchService, ResearchServiceExecutor<ScpRankListResponse> scpRankListResRse,
			AfterEvoScpRankResearchService afterEvoScpRankResearchService, ResearchServiceExecutor<AfterEvoScpRankResponse> afterEvoScpRankResRse,
			ValidationService validationService) {
		// SCPランク算出
		this.scpRankResearchService = scpRankResearchService;
		this.scpRankResRse = scpRankResRse;
		// SCPランクの最高と最低を算出
		this.scpRankMaxMinResearchService = scpRankMaxMinResearchService;
		this.scpRankMaxMinResRse = scpRankMaxMinResRse;
		// SCPランク一覧取得
		this.scpRankListResearchService = scpRankListResearchService;
		this.scpRankListResRse = scpRankListResRse;
		// 進化後SCPランク
		this.afterEvoScpRankResearchService = afterEvoScpRankResearchService;
		this.afterEvoScpRankResRse = afterEvoScpRankResRse;
		// 入力チェック
		this.validationService = validationService;
	}

	/**
	 *
	 * 対象のポケモン1個体のSCPの順位を求めます。<br>
	 * スーパーリーグ、ハイパーリーグ、マスターリーグすべて求めます。<br>
	 * INPUT：
	 * <ul>
	 *   <li>id or name</li>
	 * </ul>
	 *
	 * @param scpRequest
	 * @return
	 * @throws Exception
	 */
	@GetMapping("/scpRank")
	public ScpRankResponse scpRank(ScpRankRequest scpRankReq) throws Exception {

		validationService.validation(scpRankReq);

		ScpRankResponse scpRankRes = new ScpRankResponse();
		scpRankResRse.execute(scpRankReq, scpRankRes, scpRankResearchService);
		return scpRankRes;
	}

	/**
	 *
	 * 対象のポケモン1個体のSCPの順位の最高と最低を求めます。<br>
	 * スーパーリーグ、ハイパーリーグ、マスターリーグすべて求めます。<br>
	 * INPUT：
	 * <ul>
	 *   <li>id or name</li>
	 * </ul>
	 *
	 * @param scpRequest
	 * @return
	 * @throws Exception
	 */
	@GetMapping("/scpRankMaxMin")
	public ScpRankMaxMinResponse scpRankMaxMin(ScpRankMaxMinRequest scpMaxMinReq) throws Exception {

		validationService.validation(scpMaxMinReq);

		ScpRankMaxMinResponse scpRankMaxMinRes = new ScpRankMaxMinResponse();
		scpRankMaxMinResRse.execute(scpMaxMinReq, scpRankMaxMinRes, scpRankMaxMinResearchService);
		return scpRankMaxMinRes;
	}

	/**
	 * PvP順位一覧用API
	 *
	 * リーグに応じたSCPの順位（PvP順位）を求めます。
	 * INPUT：
	 * <ul>
	 *   <li>id or name</li>
	 *   <li>league = ("sl","gl","hl","ul","ml")</li>
	 * </ul>
	 *
	 * @param scpRankListReq
	 * @return
	 * @throws Exception
	 */
	@GetMapping("/scpRankList")
	public ScpRankListResponse scpRankList(ScpRankListRequest scpRankListReq) throws Exception {

		validationService.validation(scpRankListReq);

		ScpRankListResponse scpRankListRes = new ScpRankListResponse();
		scpRankListResRse.execute(scpRankListReq, scpRankListRes, scpRankListResearchService);
		return scpRankListRes;
	}

	/**
	 * 進化後PvP順位用API
	 *
	 * @param afterEvoScpRankListReq
	 * @return
	 * @throws Exception
	 */
	@GetMapping("/afterEvoScpRank")
	public AfterEvoScpRankResponse afterEvoScpRank(AfterEvoScpRankRequest afterEvoScpRankListReq) throws Exception {

		validationService.validation(afterEvoScpRankListReq);

		AfterEvoScpRankResponse afterEvoScpRankRes = new AfterEvoScpRankResponse();
		afterEvoScpRankResRse.execute(afterEvoScpRankListReq, afterEvoScpRankRes, afterEvoScpRankResearchService);
		return afterEvoScpRankRes;
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
