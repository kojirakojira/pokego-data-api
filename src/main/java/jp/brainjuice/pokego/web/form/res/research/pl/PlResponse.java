package jp.brainjuice.pokego.web.form.res.research.pl;

import jp.brainjuice.pokego.business.service.utils.dto.SearchValue;
import jp.brainjuice.pokego.web.form.res.research.ResearchResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class PlResponse extends ResearchResponse {

	private SearchValue individialValue;
	private String pl;
}
