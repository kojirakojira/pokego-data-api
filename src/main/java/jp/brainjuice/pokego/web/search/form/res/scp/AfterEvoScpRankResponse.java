package jp.brainjuice.pokego.web.search.form.res.scp;

import java.util.List;

import jp.brainjuice.pokego.business.service.search.utils.dto.AfterEvolIv;
import jp.brainjuice.pokego.dao.jpa.entity.GoPokedex;
import jp.brainjuice.pokego.web.search.form.res.ResearchResponse;
import jp.brainjuice.pokego.web.search.form.res.elem.ScpRankAllInOne;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper=false)
@NoArgsConstructor
public class AfterEvoScpRankResponse extends ResearchResponse {

	private GoPokedex searchPokemon;
	private int iva;
	private int ivd;
	private int ivh;
	private Integer cp;
	private String pl;
	private List<AfterEvolIv> afEvolIvList;
	private List<ScpRankAllInOne> afEvolScpRankList;
	private AfterEvolIv targetGpIv;
}
