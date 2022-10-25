package jp.brainjuice.pokego.web.form.req.research;

import lombok.Data;

@Data
public abstract class ResearchRequestImpl implements ResearchRequest {

	private String id;
	private String name;
//	private Integer iva;
//	private Integer ivd;
//	private Integer ivh;
//	private String pl;
}
