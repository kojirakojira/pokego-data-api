package jp.brainjuice.pokego.web.form.req.research;

import javax.validation.constraints.Null;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class RaceRequest extends ResearchRequestImpl {

	@Null
	private Integer iva;
	@Null
	private Integer ivd;
	@Null
	private Integer ivh;
	@Null
	private String pl;
}
