package jp.brainjuice.pokego.web.form.res.research.cp;

import jp.brainjuice.pokego.business.dao.entity.GoPokedex;
import jp.brainjuice.pokego.web.form.res.elem.CpRank;
import jp.brainjuice.pokego.web.form.res.research.ResearchResponse;
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
