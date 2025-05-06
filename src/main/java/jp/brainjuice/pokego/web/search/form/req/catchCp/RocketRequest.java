package jp.brainjuice.pokego.web.search.form.req.catchCp;

import jp.brainjuice.pokego.web.search.form.req.ResearchRequestImpl;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class RocketRequest extends ResearchRequestImpl {

	private boolean sakaki;
}
