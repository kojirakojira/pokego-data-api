package jp.brainjuice.pokego.web.form.req.research;

import javax.validation.constraints.Null;

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
