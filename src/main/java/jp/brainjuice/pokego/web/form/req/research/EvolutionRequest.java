package jp.brainjuice.pokego.web.form.req.research;

import javax.validation.constraints.Null;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper=false)
@AllArgsConstructor
@NoArgsConstructor
public class EvolutionRequest extends ResearchRequestImpl {

//	private String id;
//	private String name;
	@Null
	private Integer iva;
	@Null
	private Integer ivd;
	@Null
	private Integer ivh;
	@Null
	private String pl;
}
