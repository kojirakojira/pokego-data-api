package jp.brainjuice.pokego.utils.exception;

import java.text.MessageFormat;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

/**
 * リクエストのパラメータに誤りがある場合にスローします。
 *
 * @author saibabanagchampa
 *
 */
public class BadRequestException extends Exception {

	public BadRequestException() {
		super();
	}

	public BadRequestException(String msg) {
		super(msg);
	}

	public BadRequestException(String msg, Throwable e) {
		super(msg, e);
	}

	public BadRequestException(Throwable e) {
		super(e);
	}

	public BadRequestException(String id, String name) {
		super(getMessage(id, name));
	}

	private static String getMessage(String id, String name) {

		HttpServletRequest httpServletRequest = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
		String uri = httpServletRequest.getRequestURI();
		String page = uri.substring(uri.lastIndexOf("/") + 1, uri.length());
		return MessageFormat.format("page:{0}, id:{1}, name:{2}", page, id, name);
	}
}
