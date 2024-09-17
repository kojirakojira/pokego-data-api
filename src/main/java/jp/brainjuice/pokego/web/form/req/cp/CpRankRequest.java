package jp.brainjuice.pokego.web.form.req.cp;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jp.brainjuice.pokego.web.form.req.ResearchRequestImpl;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
@AllArgsConstructor
public class CpRankRequest extends ResearchRequestImpl {

	@NotNull
	@Min(0)
	@Max(15)
	private Integer iva;
	@NotNull
	@Min(0)
	@Max(15)
	private Integer ivd;
	@NotNull
	@Min(0)
	@Max(15)
	private Integer ivh;

}
