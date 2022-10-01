package jp.brainjuice.pokego.web.form.res.research;

import jp.brainjuice.pokego.business.service.utils.dto.IndividialValue;
import jp.brainjuice.pokego.web.form.res.Response;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class PlResponse extends Response {

	private IndividialValue individialValue;
	private String pl;
}
