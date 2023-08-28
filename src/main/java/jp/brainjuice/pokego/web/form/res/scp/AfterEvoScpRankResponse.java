package jp.brainjuice.pokego.web.form.res.scp;

import java.util.List;

import jp.brainjuice.pokego.business.dao.entity.GoPokedex;
import jp.brainjuice.pokego.business.service.utils.dto.AfterEvoIv;
import jp.brainjuice.pokego.web.form.res.ResearchResponse;
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
	private List<AfterEvoIv> afEvoList;
}
