package jp.brainjuice.pokego.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jp.brainjuice.pokego.business.service.research.ResearchServiceExecutor;
import jp.brainjuice.pokego.business.service.research.scp.ScpRankListResearchService;
import jp.brainjuice.pokego.business.service.research.scp.ScpRankMaxMinResearchService;
import jp.brainjuice.pokego.business.service.research.scp.ScpRankResearchService;
import jp.brainjuice.pokego.filter.log.LogUtils;
import jp.brainjuice.pokego.utils.exception.BadRequestException;
import jp.brainjuice.pokego.web.form.req.research.scp.ScpRankListRequest;
import jp.brainjuice.pokego.web.form.req.research.scp.ScpRankMaxMinRequest;
import jp.brainjuice.pokego.web.form.req.research.scp.ScpRankRequest;
import jp.brainjuice.pokego.web.form.res.research.scp.ScpRankListResponse;
import jp.brainjuice.pokego.web.form.res.research.scp.ScpRankMaxMinResponse;
import jp.brainjuice.pokego.web.form.res.research.scp.ScpRankResponse;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000")
public class ScpResearchController {

	private ScpRankResearchService scpRankResearchService;
	private ResearchServiceExecutor<ScpRankResponse> scpRankResRse;

	private ScpRankMaxMinResearchService scpRankMaxMinResearchService;
	private ResearchServiceExecutor<ScpRankMaxMinResponse> scpRankMaxMinResRse;

	private ScpRankListResearchService scpRankListResearchService;
	private ResearchServiceExecutor<ScpRankListResponse> scpRankListResRse;


	@Autowired
	public ScpResearchController(
			ScpRankResearchService scpRankResearchService, ResearchServiceExecutor<ScpRankResponse> scpRankResRse,
			ScpRankMaxMinResearchService scpRankMaxMinResearchService, ResearchServiceExecutor<ScpRankMaxMinResponse> scpRankMaxMinResRse,
			ScpRankListResearchService scpRankListResearchService, ResearchServiceExecutor<ScpRankListResponse> scpRankListResRse) {
		// SCPランク算出
		this.scpRankResearchService = scpRankResearchService;
		this.scpRankResRse = scpRankResRse;
		// SCPランクの最高と最低を算出
		this.scpRankMaxMinResearchService = scpRankMaxMinResearchService;
		this.scpRankMaxMinResRse = scpRankMaxMinResRse;
		// SCPランク一覧取得
		this.scpRankListResearchService = scpRankListResearchService;
		this.scpRankListResRse = scpRankListResRse;
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
	 * @throws BadRequestException
	 */
	@GetMapping("/scpRank")
	public ScpRankResponse scpRank(@Validated ScpRankRequest scpRankReq) throws BadRequestException {

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
	 * @throws BadRequestException
	 */
	@GetMapping("/scpRankMaxMin")
	public ScpRankMaxMinResponse scpRankMaxMin(@Validated ScpRankMaxMinRequest scpMaxMinReq) throws BadRequestException {

		ScpRankMaxMinResponse scpRankMaxMinRes = new ScpRankMaxMinResponse();
		scpRankMaxMinResRse.execute(scpMaxMinReq, scpRankMaxMinRes, scpRankMaxMinResearchService);
		return scpRankMaxMinRes;
	}

	/**
	 *
	 * リーグに応じたSCPの順位（pvp順位）を求めます。
	 * INPUT：
	 * <ul>
	 *   <li>id or name</li>
	 *   <li>league("sl","gl","hl","ul","ml")</li>
	 * </ul>
	 *
	 * @param scpRankListReq
	 * @return
	 * @throws BadRequestException
	 */
	@GetMapping("/scpRankList")
	public ScpRankListResponse scpRankList(@Validated ScpRankListRequest scpRankListReq) throws BadRequestException {

		ScpRankListResponse scpRankListRes = new ScpRankListResponse();
		scpRankListResRse.execute(scpRankListReq, scpRankListRes, scpRankListResearchService);
		return scpRankListRes;
	}

	@ExceptionHandler(BadRequestException.class)
	public ResponseEntity<String> badRequestException(Exception e) {
		String errMsg = "不正なアクセスです。";
		LogUtils.getLog(this).error(errMsg, e);
		return new ResponseEntity<String>(errMsg, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<String> exception(Exception e) {
		String errMsg = "処理中に想定外の問題が発生しました。";
		LogUtils.getLog(this).error(errMsg, e);
		return new ResponseEntity<String>(errMsg, HttpStatus.INTERNAL_SERVER_ERROR);
	}

}
