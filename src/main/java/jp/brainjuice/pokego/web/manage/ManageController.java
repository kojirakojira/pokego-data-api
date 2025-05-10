package jp.brainjuice.pokego.web.manage;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jp.brainjuice.pokego.business.service.manage.LoginService;
import jp.brainjuice.pokego.cache.service.ViewsCacheProvider;
import jp.brainjuice.pokego.filter.jwt.BjJwtUtils;
import jp.brainjuice.pokego.utils.exception.AuthenticationFailedException;
import jp.brainjuice.pokego.utils.exception.BadRequestException;
import jp.brainjuice.pokego.utils.exception.UserUnmatchException;
import jp.brainjuice.pokego.web.manage.req.LoginRequest;
import jp.brainjuice.pokego.web.manage.res.LoginResponse;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api")
@Slf4j
public class ManageController {
	
	private LoginService loginService;
	
	private ViewsCacheProvider viewsCacheProvider;
	
	public ManageController(
			LoginService loginService,
			ViewsCacheProvider viewsCacheProvider) {
		this.loginService = loginService;
		this.viewsCacheProvider = viewsCacheProvider;
	}

	@PostMapping("/secure/cleanupRedis")
	public String cleanUpRedis(String userId,
			HttpServletRequest req) throws Exception {
		if (!BjJwtUtils.checkUser(req, userId)) {
			throw new UserUnmatchException();
		}

		viewsCacheProvider.cleanupPageTempView();
		viewsCacheProvider.cleanupPokemonTempView();
		
		return "OK";
	}

	@PostMapping("/secure/manage")
	public boolean index(String userId,
			HttpServletRequest req) throws Exception {
		if (!BjJwtUtils.checkUser(req, userId)) {
			throw new UserUnmatchException();
		}
		return true;
	}

	@PostMapping("/manage/login")
	public LoginResponse login(@Valid LoginRequest loginReq,
			HttpServletRequest req) throws Exception {
		
		LoginResponse res = loginService.login(
				loginReq.getUserId(), 
				loginReq.getPassword(),
				loginReq.getMfaCode(),
				req);
		
		return res;
	}

	@ExceptionHandler(BadRequestException.class)
	public ResponseEntity<String> badRequestException(Exception e) {
		String errMsg = "不正なリクエストです。";
		log.error(errMsg, e);
		return new ResponseEntity<String>(errMsg, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(AuthenticationFailedException.class)
	public ResponseEntity<String> authenticationFailedException(Exception e) {
		String errMsg = "認証に失敗しました。";
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
