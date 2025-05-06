package jp.brainjuice.pokego.web.search.form.res.cp;

import jp.brainjuice.pokego.dao.jpa.entity.GoPokedex;
import jp.brainjuice.pokego.web.search.form.res.ResearchResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper=false)
@NoArgsConstructor
public class CpResponse extends ResearchResponse {

	private GoPokedex goPokedex;
	private int iva;
	private int ivd;
	private int ivh;
	private String pl;
	private int cp;
}
