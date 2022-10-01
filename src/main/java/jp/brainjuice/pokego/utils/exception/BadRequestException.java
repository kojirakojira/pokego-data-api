package jp.brainjuice.pokego.utils.exception;

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
}
