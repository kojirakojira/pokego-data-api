package jp.brainjuice.pokego.web;

import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import jp.brainjuice.pokego.utils.exception.BadRequestException;
import jp.brainjuice.pokego.web.search.form.res.MsgLevelEnum;
import jp.brainjuice.pokego.web.validation.form.res.NotValidResponse;
import lombok.extern.slf4j.Slf4j;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

	private MessageSource messageSource;

	public GlobalExceptionHandler(MessageSource messageSource) {
		this.messageSource = messageSource;
	}

	/**
	 * @Valid実行時の入力チェックエラーを返却する。
	 * @param ex
	 * @return
	 */
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<NotValidResponse> handleValidationExceptions(
			MethodArgumentNotValidException ex) {

		Locale currentLocale = LocaleContextHolder.getLocale();
		NotValidResponse res = new NotValidResponse();
		Map<String, String> errors = ex.getBindingResult().getFieldErrors().stream()
				.map(error -> {
					// 埋め込み文字の適用
					String resolvedMessage = messageSource.getMessage(
							error.getCode(), // 1. エラーコード (例: NotNull)
							error.getArguments(), // 2. 埋め込み引数 (例: {0}の値, {min}の値)
							error.getDefaultMessage(), // 3. デフォルトテンプレート
							currentLocale);
					return Map.entry(error.getField(), resolvedMessage);
				})
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
		res.setErrors(errors);
		Set<String> messages = new TreeSet<>(errors.values());
		res.setMessages(messages);

		res.setSuccess(false);
		res.setMsgLevel(MsgLevelEnum.warn);

		return new ResponseEntity<>(res, HttpStatus.UNPROCESSABLE_ENTITY);
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
