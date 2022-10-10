package jp.brainjuice.pokego.web;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jp.brainjuice.pokego.business.service.research.ResearchServiceExecutor;
import jp.brainjuice.pokego.business.service.research.cp.CpRankListResearchService;
import jp.brainjuice.pokego.business.service.research.cp.CpRankResearchService;
import jp.brainjuice.pokego.business.service.research.cp.CpResearchService;
import jp.brainjuice.pokego.business.service.research.cp.RaidResearchService;
import jp.brainjuice.pokego.filter.log.LogUtils;
import jp.brainjuice.pokego.utils.exception.BadRequestException;
import jp.brainjuice.pokego.web.form.req.research.cp.CpRankListRequest;
import jp.brainjuice.pokego.web.form.req.research.cp.CpRankRequest;
import jp.brainjuice.pokego.web.form.req.research.cp.CpRequest;
import jp.brainjuice.pokego.web.form.req.research.cp.RaidRequest;
import jp.brainjuice.pokego.web.form.res.research.cp.CpRankListResponse;
import jp.brainjuice.pokego.web.form.res.research.cp.CpRankResponse;
import jp.brainjuice.pokego.web.form.res.research.cp.CpResponse;
import jp.brainjuice.pokego.web.form.res.research.cp.RaidResponse;

@RestController
@RequestMapping("/api")
public class CpResearchController {

	private CpResearchService cpResearchService;
	private ResearchServiceExecutor<CpResponse> cpResRse;

	private CpRankResearchService cpRankResearchService;
	private ResearchServiceExecutor<CpRankResponse> cpRankResRse;

	private CpRankListResearchService cpRankListResearchService;
	private ResearchServiceExecutor<CpRankListResponse> cpRankListResRse;

	private RaidResearchService raidResearchService;
	private ResearchServiceExecutor<RaidResponse> raidResRse;

	@Autowired
	public CpResearchController(
			CpResearchService cpResearchService, ResearchServiceExecutor<CpResponse> cpResRse,
			CpRankResearchService cpRankResearchService, ResearchServiceExecutor<CpRankResponse> cpRankResRse,
			CpRankListResearchService cpRankListResearchService, ResearchServiceExecutor<CpRankListResponse> cpRankListResRse,
			ResearchServiceExecutor<RaidResponse> raidResRse, RaidResearchService raidResearchService) {
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
	 */
	@GetMapping("/cp")
	public CpResponse cp(@Validated CpRequest cpReq) throws BadRequestException {

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
	 * @throws BadRequestException
	 */
	@GetMapping("/cpRank")
	public CpRankResponse cpRank(@Validated CpRankRequest cpRankReq) throws BadRequestException {

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
	 * @throws BadRequestException
	 */
	@GetMapping("/cpRankList")
	public CpRankListResponse cpRankList(@Validated CpRankListRequest cpRankListReq) throws BadRequestException {

		CpRankListResponse cpRankListRes = new CpRankListResponse();
		cpRankListResRse.execute(cpRankListReq, cpRankListRes, cpRankListResearchService);
		return cpRankListRes;
	}


	@GetMapping("/raid")
	public RaidResponse raid(@Validated RaidRequest raidReq, HttpServletRequest req) throws BadRequestException {

		RaidResponse raidRes = new RaidResponse();
		raidResRse.execute(raidReq, raidRes, raidResearchService);
		return raidRes;
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
