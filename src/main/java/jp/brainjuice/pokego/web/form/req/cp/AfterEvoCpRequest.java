package jp.brainjuice.pokego.web.form.req.cp;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import jp.brainjuice.pokego.web.form.req.ResearchRequestImpl;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper=false)
public class AfterEvoCpRequest extends ResearchRequestImpl {

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
	@NotNull
	@Min(0)
	private Integer cp;
}
