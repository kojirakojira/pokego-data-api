package jp.brainjuice.pokego.web.search.form.res.catchCp;

import jp.brainjuice.pokego.web.search.form.res.ResearchResponse;
import jp.brainjuice.pokego.web.search.form.res.elem.CatchCp;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper=false)
@AllArgsConstructor
@NoArgsConstructor
public class WildResponse extends ResearchResponse {

	private CatchCp catchCp;
}
