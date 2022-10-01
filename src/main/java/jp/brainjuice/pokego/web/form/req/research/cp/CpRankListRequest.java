package jp.brainjuice.pokego.web.form.req.research.cp;

import javax.validation.constraints.Null;

import jp.brainjuice.pokego.web.form.req.Request;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper=false)
@AllArgsConstructor
@NoArgsConstructor
public class CpRankListRequest extends Request {

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
