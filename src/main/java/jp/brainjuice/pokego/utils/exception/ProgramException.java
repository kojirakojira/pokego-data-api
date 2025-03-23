package jp.brainjuice.pokego.utils.exception;

/**
 * プログラムの実装に問題がある場合に発生させる例外
 */
public class ProgramException extends RuntimeException {


	public ProgramException() {
		super("プログラムの実装に誤りがあります");
	}

	public ProgramException(String msg) {
		super(msg);
	}

	public ProgramException(String msg, Throwable e) {
		super(msg, e);
	}

	public ProgramException(Throwable e) {
		super(e);
	}
}
