package jp.brainjuice.pokego.web.search.form.req.cp;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import jp.brainjuice.pokego.web.search.form.req.ResearchRequestImpl;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
@AllArgsConstructor
public class CpRankRequest extends ResearchRequestImpl {

	@Min(0)
	@Max(15)
	private int iva;
	@Min(0)
	@Max(15)
	private int ivd;
	@Min(0)
	@Max(15)
	private int ivh;

}
