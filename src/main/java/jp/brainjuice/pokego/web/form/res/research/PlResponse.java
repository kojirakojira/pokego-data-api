package jp.brainjuice.pokego.web.form.res.research;

import jp.brainjuice.pokego.business.service.utils.dto.IndividialValue;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class PlResponse extends ResearchResponse {

	private IndividialValue individialValue;
	private String pl;
}
