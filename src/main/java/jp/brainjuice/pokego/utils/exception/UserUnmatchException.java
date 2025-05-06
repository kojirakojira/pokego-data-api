package jp.brainjuice.pokego.utils.exception;

public class UserUnmatchException extends Exception {

	public UserUnmatchException() {
		super();
	}

	public UserUnmatchException(String msg) {
		super(msg);
	}

	public UserUnmatchException(String msg, Throwable e) {
		super(msg, e);
	}
}
