package jp.brainjuice.pokego.web.search.form.req.race;

import jp.brainjuice.pokego.web.search.form.req.ResearchRequestImpl;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class RaceRequest extends ResearchRequestImpl {

	private boolean statsRequired;
}
