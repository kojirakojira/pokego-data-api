package jp.brainjuice.pokego.web.form.req.research.scp;

import javax.validation.constraints.Null;

import jp.brainjuice.pokego.web.form.req.research.ResearchRequestImpl;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper=false)
@AllArgsConstructor
@NoArgsConstructor
public class ScpRankMaxMinRequest extends ResearchRequestImpl {

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
