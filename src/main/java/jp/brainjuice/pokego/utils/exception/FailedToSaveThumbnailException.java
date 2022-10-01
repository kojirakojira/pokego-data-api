package jp.brainjuice.pokego.utils.exception;

public class FailedToSaveThumbnailException extends Exception {

	public FailedToSaveThumbnailException() {
		super();
	}

	public FailedToSaveThumbnailException(String msg) {
		super(msg);
	}

	public FailedToSaveThumbnailException(String msg, Throwable e) {
		super(msg, e);
	}

	public FailedToSaveThumbnailException(Throwable e) {
		super(e);
	}
}
