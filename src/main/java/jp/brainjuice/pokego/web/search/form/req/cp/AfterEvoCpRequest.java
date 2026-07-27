package jp.brainjuice.pokego.web.search.form.req.cp;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import jp.brainjuice.pokego.web.search.form.req.ResearchRequestImpl;
import jp.brainjuice.pokego.web.validation.InRange;
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
	@InRange(min = 0, max = 15)
	private int iva;
	@NotNull
	@InRange(min = 0, max = 15)
	private int ivd;
	@NotNull
	@InRange(min = 0, max = 15)
	private int ivh;
	@Min(0)
	private Integer cp;
}
