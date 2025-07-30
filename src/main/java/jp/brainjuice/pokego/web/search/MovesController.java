package jp.brainjuice.pokego.web.search;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jp.brainjuice.pokego.business.service.search.moves.MoveListService;
import jp.brainjuice.pokego.cache.service.ViewsCacheProvider;
import jp.brainjuice.pokego.utils.exception.BadRequestException;
import jp.brainjuice.pokego.web.search.form.res.moves.MoveListResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * 技の情報を取得するコントローラクラス
 *
 * @author saibabanagchampa
 *
 */
@RestController
@RequestMapping("/api")
@Slf4j
public class MovesController {

	private MoveListService moveListService;

	private ViewsCacheProvider viewsCacheProvider;

	public MovesController(
			MoveListService moveListService,
			ViewsCacheProvider viewsCacheProvider) {
		this.moveListService = moveListService;
		this.viewsCacheProvider = viewsCacheProvider;
	}

	/**
	 * @return
	 */
//	@GetMapping("/moveListPattern")
//	public LinkedHashMap<String, String> moveListPattern() {
//
//		return Stream.of(MoveListPatternEnum.values())
//				.collect(Collectors.toMap(
//						itrs -> itrs.name(),
//						itrs -> itrs.getJpn(),
//						(a, b) -> a,
//						LinkedHashMap::new));
//	}

	@GetMapping("/moveList")
	public MoveListResponse moveList() {

		MoveListResponse res = new MoveListResponse();

		moveListService.exec(res);

		viewsCacheProvider.addTempList();

		return res;
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
