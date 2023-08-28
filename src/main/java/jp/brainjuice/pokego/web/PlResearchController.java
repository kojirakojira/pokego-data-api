package jp.brainjuice.pokego.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jp.brainjuice.pokego.business.service.ResearchServiceExecutor;
import jp.brainjuice.pokego.business.service.pl.PlListResearchService;
import jp.brainjuice.pokego.business.service.pl.PlResearchService;
import jp.brainjuice.pokego.utils.exception.BadRequestException;
import jp.brainjuice.pokego.web.form.req.pl.PlListRequest;
import jp.brainjuice.pokego.web.form.req.pl.PlRequest;
import jp.brainjuice.pokego.web.form.res.pl.PlListResponse;
import jp.brainjuice.pokego.web.form.res.pl.PlResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * PLに関する情報を取得するためのコントローラクラス
 *
 * @author saibabanagchampa
 *
 */
@RestController
@RequestMapping("/api")
@Slf4j
public class PlResearchController {

	private PlResearchService plResearchService;
	private ResearchServiceExecutor<PlResponse> plResRse;

	private PlListResearchService plListResearchService;
	private ResearchServiceExecutor<PlListResponse> plListResRse;

	@Autowired
	public PlResearchController(
			PlResearchService plResearchService, ResearchServiceExecutor<PlResponse> plResRse,
			PlListResearchService plListResearchService, ResearchServiceExecutor<PlListResponse> plListResRse) {
		// PL算出
		this.plResearchService = plResearchService;
		this.plResRse = plResRse;
		// PLごとのCP一覧
		this.plListResearchService = plListResearchService;
		this.plListResRse = plListResRse;
	}

	/**
	 * PL算出用API（ivとcpからPLを求める。）
	 *
	 * @param plReq
	 * @return
	 * @throws BadRequestException
	 */
	@GetMapping("/pl")
	public PlResponse pl(PlRequest plReq) throws BadRequestException {

		PlResponse plRes = new PlResponse();
		plResRse.execute(plReq, plRes, plResearchService);
		return plRes;
	}

	/**
	 * PLごとのCP一覧用API
	 *
	 * @param plReq
	 * @return
	 * @throws BadRequestException
	 */
	@GetMapping("/plList")
	public PlListResponse plListList(PlListRequest plListReq) throws BadRequestException {

		PlListResponse plListRes = new PlListResponse();
		plListResRse.execute(plListReq, plListRes, plListResearchService);
		return plListRes;
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
