package jp.brainjuice.pokego.utils.exception;

/**
 * データの整合性に問題がある場合にthrowする例外クラス
 * 基本的には発生しないものである。
 * 被検査例外として定義する。
 */
public class PokemonDataException extends RuntimeException {

	public PokemonDataException() {
		super();
	}

	public PokemonDataException(String msg) {
		super(msg);
	}

	public PokemonDataException(String msg, Throwable e) {
		super(msg, e);
	}

	public PokemonDataException(Throwable e) {
		super(e);
	}
}
