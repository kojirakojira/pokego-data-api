package jp.brainjuice.pokego.web.form.req.scp;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jp.brainjuice.pokego.web.form.req.ResearchRequestImpl;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper=false)
public class AfterEvoScpRankRequest extends ResearchRequestImpl {

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

	private Integer cp;
}
