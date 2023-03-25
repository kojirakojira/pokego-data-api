package jp.brainjuice.pokego.web.form.req.research.others;

import jp.brainjuice.pokego.web.form.req.research.ResearchRequestImpl;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class TypeScoreRequest extends ResearchRequestImpl {

	private String type1;
	private String type2;
}
