package jp.brainjuice.pokego.web.form.res.research.cp;

import java.util.List;

import jp.brainjuice.pokego.business.dao.entity.GoPokedex;
import jp.brainjuice.pokego.business.service.utils.dto.GoPokedexAndCp;
import jp.brainjuice.pokego.web.form.res.research.ResearchResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper=false)
@NoArgsConstructor
public class AfterEvoCpResponse extends ResearchResponse {

	private GoPokedex searchPokemon;
	private int iva;
	private int ivd;
	private int ivh;
	private int cp;
	private String pl;
	private List<GoPokedexAndCp> afEvoList;
	private List<GoPokedexAndCp> anotherFormList;
}
