package jp.brainjuice.pokego.web.search.form.req.pl;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import jp.brainjuice.pokego.web.search.form.req.ResearchRequestImpl;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class PlRequest extends ResearchRequestImpl {

	@Min(0)
	@Max(15)
	private int iva;
	@Min(0)
	@Max(15)
	private int ivd;
	@Min(0)
	@Max(15)
	private int ivh;
	@Min(0)
	@Max(15)
	private int cp;
}
