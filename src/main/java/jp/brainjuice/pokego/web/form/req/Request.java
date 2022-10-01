package jp.brainjuice.pokego.web.form.req;

import lombok.Data;

@Data
public abstract class Request {

	private String id;
	private String name;
	private Integer iva;
	private Integer ivd;
	private Integer ivh;
	private String pl;
}
