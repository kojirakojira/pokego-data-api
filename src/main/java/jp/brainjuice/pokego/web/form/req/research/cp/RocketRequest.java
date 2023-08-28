package jp.brainjuice.pokego.web.form.req.research.cp;

import jp.brainjuice.pokego.web.form.req.research.ResearchRequestImpl;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class RocketRequest extends ResearchRequestImpl {

	private boolean sakaki;
}
