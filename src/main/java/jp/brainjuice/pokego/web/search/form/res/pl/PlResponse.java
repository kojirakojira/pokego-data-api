package jp.brainjuice.pokego.web.search.form.res.pl;

import jp.brainjuice.pokego.business.service.search.pokeFilter.dto.SearchValue;
import jp.brainjuice.pokego.web.search.form.res.ResearchResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class PlResponse extends ResearchResponse {

	private SearchValue individialValue;
	private String pl;
}
