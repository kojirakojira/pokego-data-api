package jp.brainjuice.pokego.web.form.req.research.pl;

import javax.validation.constraints.NotNull;

import jp.brainjuice.pokego.web.form.req.research.ResearchRequestImpl;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class PlRequest extends ResearchRequestImpl {

	@NotNull
	private Integer iva;
	@NotNull
	private Integer ivd;
	@NotNull
	private Integer ivh;
	@NotNull
	private int cp;
}
