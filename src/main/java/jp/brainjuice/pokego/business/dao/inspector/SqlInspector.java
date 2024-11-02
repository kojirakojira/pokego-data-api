package jp.brainjuice.pokego.business.dao.inspector;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.hibernate.resource.jdbc.spi.StatementInspector;

import lombok.extern.slf4j.Slf4j;

/**
 * SQLの呼び出し元をログ出力する。<br>
 * {@link @Aspect}を使用せずに処理が差し込まれているため注意すること。
 */
@Slf4j
public class SqlInspector implements StatementInspector {

	@Override
	public String inspect(String sql) {
		// 呼び出し元のスタックトレースを取得
		StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();

		// スタックトレースの任意の箇所から呼び出し元のクラス名・メソッド名を取得
		String caller = Arrays.stream(stackTraceElements)
				.filter(ste -> ste.getClassName().contains("jp.brainjuice.pokego"))
				.filter(ste -> !ste.getClassName().contains("jp.brainjuice.pokego.business.dao.inspector"))
				.map(StackTraceElement::toString)
				.collect(Collectors.joining(" -> "));

		log.debug("SQL called by: " + caller);

		return sql;  // 変更せずにそのまま返す
	}
}