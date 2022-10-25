package jp.brainjuice.pokego.utils.exception;

/**
 * ポケモン情報の初期化処理に失敗した場合にスローします。
 *
 * @author saibabanagchampa
 *
 */
public class PokemonDataInitException extends Exception {

	public PokemonDataInitException() {
		super();
	}

	public PokemonDataInitException(String msg) {
		super(msg);
	}

	public PokemonDataInitException(String msg, Throwable e) {
		super(msg, e);
	}

	public PokemonDataInitException(Throwable e) {
		super(e);
	}
}
