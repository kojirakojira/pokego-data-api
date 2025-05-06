package jp.brainjuice.pokego.web.search.form.res.cp;

import jp.brainjuice.pokego.dao.jpa.entity.GoPokedex;
import jp.brainjuice.pokego.web.search.form.res.ResearchResponse;
import jp.brainjuice.pokego.web.search.form.res.elem.CpRank;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class CpRankResponse extends ResearchResponse {

	private GoPokedex goPokedex;
	private int iva;
	private int ivd;
	private int ivh;
	private CpRank cpRank;
}
