package jp.brainjuice.pokego.utils.exception;

/**
 * 認証失敗時にスローする例外
 */
public class AuthenticationFailedException extends RuntimeException {

	public AuthenticationFailedException() {
		super();
	}

	public AuthenticationFailedException(String msg) {
		super(msg);
	}

	public AuthenticationFailedException(String msg, Throwable e) {
		super(msg, e);
	}

	public AuthenticationFailedException(Throwable e) {
		super(e);
	}
}
