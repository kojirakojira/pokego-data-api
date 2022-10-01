package jp.brainjuice.pokego.web.form.req.research.cp;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

import jp.brainjuice.pokego.web.form.req.Request;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
@AllArgsConstructor
public class CpRankRequest extends Request {

//	private String id;
//	private String name;
	@NotNull
	private Integer iva;
	@NotNull
	private Integer ivd;
	@NotNull
	private Integer ivh;
	@Null
	private String pl;

}
