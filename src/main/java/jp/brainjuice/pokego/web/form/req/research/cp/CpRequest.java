package jp.brainjuice.pokego.web.form.req.research.cp;

import javax.validation.constraints.NotNull;

import jp.brainjuice.pokego.web.form.req.Request;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper=false)
public class CpRequest extends Request {

//	private String id;
//	private String name;
	@NotNull
	private Integer iva;
	@NotNull
	private Integer ivd;
	@NotNull
	private Integer ivh;
	@NotNull
	private String pl;
}
