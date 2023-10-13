package jp.brainjuice.pokego.web.form.req.others;

import javax.validation.constraints.NotNull;

import jp.brainjuice.pokego.web.form.req.ResearchRequestImpl;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class EvoCostRequest extends ResearchRequestImpl {

	@NotNull
	private String costs;
}
