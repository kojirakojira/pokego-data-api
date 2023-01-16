package jp.brainjuice.pokego.utils.exception;

import jp.brainjuice.pokego.utils.BjCsvMapper;

/**
 *
 * BjCsvMapperクラスでCsvをBeanに変換しようとして失敗した場合にスローします。
 *
 * @author saibabanagchampa
 * @see BjCsvMapper
 *
 */
public class CsvMappingException extends Exception {

	public CsvMappingException() {
		super();
	}

	public CsvMappingException(String msg) {
		super(msg);
	}

	public CsvMappingException(String msg, Throwable e) {
		super(msg, e);
	}

	public CsvMappingException(Throwable e) {
		super(e);
	}
}
