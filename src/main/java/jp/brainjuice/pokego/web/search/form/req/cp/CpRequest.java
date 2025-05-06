package jp.brainjuice.pokego.web.search.form.req.cp;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import jp.brainjuice.pokego.web.search.form.req.ResearchRequestImpl;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper=false)
public class CpRequest extends ResearchRequestImpl {

	@Min(0)
	@Max(15)
	private Integer iva;
	@Min(0)
	@Max(15)
	private Integer ivd;
	@Min(0)
	@Max(15)
	private Integer ivh;
	@NotNull
	private String pl;
}
