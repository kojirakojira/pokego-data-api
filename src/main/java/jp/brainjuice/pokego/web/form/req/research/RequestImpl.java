package jp.brainjuice.pokego.web.form.req.research;

import jp.brainjuice.pokego.web.form.req.Request;
import lombok.Data;

@Data
public abstract class RequestImpl implements Request {

	private String id;
	private String name;
//	private Integer iva;
//	private Integer ivd;
//	private Integer ivh;
//	private String pl;
}
