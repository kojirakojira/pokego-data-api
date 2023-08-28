package jp.brainjuice.pokego.web.form.req.type;

import jp.brainjuice.pokego.web.form.req.ResearchRequestImpl;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class TypeScoreRequest extends ResearchRequestImpl {

	private String type1;
	private String type2;
}
