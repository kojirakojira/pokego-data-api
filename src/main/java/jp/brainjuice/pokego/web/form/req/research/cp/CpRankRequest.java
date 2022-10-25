package jp.brainjuice.pokego.web.form.req.research.cp;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

import jp.brainjuice.pokego.web.form.req.research.ResearchRequestImpl;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
@AllArgsConstructor
public class CpRankRequest extends ResearchRequestImpl {

//	private String id;
//	private String name;
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
	@Null
	private String pl;

}
