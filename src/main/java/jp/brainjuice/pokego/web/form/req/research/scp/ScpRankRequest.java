package jp.brainjuice.pokego.web.form.req.research.scp;

import javax.validation.constraints.NotNull;
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
public class ScpRankRequest extends Request {

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
