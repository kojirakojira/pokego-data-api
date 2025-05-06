package jp.brainjuice.pokego.web;

import org.springframework.boot.autoconfigure.web.servlet.error.AbstractErrorController;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ExceptionController extends AbstractErrorController {
	
	public ExceptionController(ErrorAttributes errorAttributes) {
		super(errorAttributes);
		// TODO 自動生成されたコンストラクター・スタブ
	}
	
	/**
	 * JWTの有効期限が切れた場合
	 * 
	 * @return
	 */
	@PostMapping("/jwtExpiredErrorFilter")
	public ResponseEntity<String> jwtExpiredErrorFilter() {
		return new ResponseEntity<String>("再ログインしてください。", HttpStatus.UNAUTHORIZED);
	}

	/**
	 * JwtAuthorizationFilterでトークン処理時にエラーが発生した場合
	 *
	 * @param exception
	 * @return
	 */
	@PostMapping("/errorFilter")
	public ResponseEntity<String> errorFilter(@RequestParam String exception) {
		return new ResponseEntity<String>("An error occurred during token processing. detail:" + exception, HttpStatus.INTERNAL_SERVER_ERROR);
	}
}
