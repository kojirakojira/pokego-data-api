package jp.brainjuice.pokego.web.form.res.research;

import jp.brainjuice.pokego.business.dao.entity.GoPokedex;
import jp.brainjuice.pokego.business.dao.entity.Pokedex;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class RaceResponse extends ResearchResponse {

	private Pokedex pokedex;
	private GoPokedex goPokedex;
	private String color;

	private int maxAt;
	private int minAt;
	private int medAt;

	private int maxDf;
	private int minDf;
	private int medDf;

	private int maxHp;
	private int minHp;
	private int medHp;
}
