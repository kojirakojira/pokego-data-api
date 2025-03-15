package jp.brainjuice.pokego.filter.log;

import java.util.concurrent.TimeUnit;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.web.filter.AbstractRequestLoggingFilter;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RequestLoggingFilter extends AbstractRequestLoggingFilter {
	
	private static final String ATTRIBUTE_KEY = "REQUEST_START_TIME";
	
	@Override
	protected boolean shouldLog(HttpServletRequest request) {
		return log.isDebugEnabled();
	}

	@Override
	protected void beforeRequest(HttpServletRequest request, String message) {
		// 開始ログは出力しない
//		log.debug(message);
		request.setAttribute(ATTRIBUTE_KEY, System.nanoTime());
	}

	@Override
	protected void afterRequest(HttpServletRequest request, String message) {
		Object startTime = request.getAttribute(ATTRIBUTE_KEY);
		if (startTime instanceof Long) {
			long endTime = System.nanoTime();
			long duration = TimeUnit.NANOSECONDS.toMillis(endTime - (long) startTime);
			log.debug("Request compulete. (Duration time: {} ms) {}", duration, message);
		} else {
			log.debug(message);
		}
	}

}
