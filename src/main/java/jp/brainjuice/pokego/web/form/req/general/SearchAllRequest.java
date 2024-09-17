package jp.brainjuice.pokego.web.form.req.general;

import jakarta.validation.constraints.Null;
import jp.brainjuice.pokego.web.form.req.ResearchRequestImpl;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 絞り込み検索用のリクエスト
 * idとnameに@Nullを付けているが、明示化させるために付けているだけである。
 *
 */
@Data
@EqualsAndHashCode(callSuper=false)
public class SearchAllRequest extends ResearchRequestImpl {

	@Null
	private String id;
}
