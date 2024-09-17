package jp.brainjuice.pokego.web.form.req.pl;

import jakarta.validation.constraints.NotNull;
import jp.brainjuice.pokego.web.form.req.ResearchRequestImpl;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class PlListRequest extends ResearchRequestImpl {

	@NotNull
	private Integer iva;
	@NotNull
	private Integer ivd;
	@NotNull
	private Integer ivh;
}
