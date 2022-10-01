package jp.brainjuice.pokego.filter.firebase.exception;

import org.springframework.security.core.AuthenticationException;

/**
 * firebaseでの認証に失敗した場合のExceptionクラス
 *
 * @author amuka
 *
 */
public class FirebaseAuthenticationException extends AuthenticationException {

	public FirebaseAuthenticationException(String msg, Throwable t) {
		super(msg, t);
	}

	public FirebaseAuthenticationException(String msg) {
		super(msg);
	}

	public FirebaseAuthenticationException() {
		super("firebase認証に失敗しました。");
	}

}
