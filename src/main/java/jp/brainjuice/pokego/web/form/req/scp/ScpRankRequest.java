package jp.brainjuice.pokego.web.form.req.scp;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jp.brainjuice.pokego.web.form.req.ResearchRequestImpl;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper=false)
@AllArgsConstructor
@NoArgsConstructor
public class ScpRankRequest extends ResearchRequestImpl {

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
