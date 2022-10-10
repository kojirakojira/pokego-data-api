package jp.brainjuice.pokego.filter.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.HandlerInterceptor;

import jp.brainjuice.pokego.utils.BjJwtUtils;
import lombok.extern.slf4j.Slf4j;


/**
 * インタセプタ
 *
 * @author amuka
 *
 */
@Slf4j
public class BrainJuiceInterceptor implements HandlerInterceptor {

	private static final String UID = "UID:";

	private static final String START = "Request arrives. ";

	private static final String END = "End ";

	private static final String REQ_PATH = "RequestDispatcherPath:";

	private static final String SEMICOLON = "; ";

	@Override
	public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) throws Exception {

		// TODO: これがWebSocket以外でも使えてるのか確認。
		String tokenUserId = BjJwtUtils.getTokenUserId(req);
		String logMsg = START + UID + tokenUserId + SEMICOLON + REQ_PATH + req.getRequestURI();
		if (!"/home".equals(req.getRequestURI())) {
			log.info(logMsg);
		} else {
			log.debug(logMsg);

		}

		return true;
	}

	@Override
	public void afterCompletion(HttpServletRequest req, HttpServletResponse res, Object handler,
			Exception ex) throws Exception {

		String tokenUserId = BjJwtUtils.getTokenUserId(req);
		String logMsg = END + UID + tokenUserId + SEMICOLON + REQ_PATH + req.getRequestURI();
		if (!"/home".equals(req.getRequestURI())) {
			log.info(logMsg);
		} else {
			log.debug(logMsg);

		}
	}
}
