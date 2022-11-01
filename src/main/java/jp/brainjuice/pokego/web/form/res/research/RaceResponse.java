package jp.brainjuice.pokego.web.form.res.research;

import jp.brainjuice.pokego.business.dao.entity.GoPokedex;
import jp.brainjuice.pokego.business.dao.entity.Pokedex;
import jp.brainjuice.pokego.business.service.utils.memory.PokemonStatisticsInfo;
import jp.brainjuice.pokego.web.form.res.elem.Color;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class RaceResponse extends ResearchResponse {

	private Pokedex pokedex;
	private GoPokedex goPokedex;
	private Color color;
	private Color type1Color;
	private Color type2Color;
	private PokemonStatisticsInfo statistics;
}
