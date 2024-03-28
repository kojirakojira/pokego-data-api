package jp.brainjuice.pokego.web.form.res.elem;

import jp.brainjuice.pokego.business.dao.entity.GoPokedex;
import jp.brainjuice.pokego.business.dao.entity.Pokedex;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 種族値を表現するクラス
 *
 * @author saibabanagchampa
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Race {

	private String pokedexId;
	private String name;
	private String remarks;
	private Pokedex pokedex;
	private GoPokedex goPokedex;
//	private Color color;
//	private Color type1Color;
//	private Color type2Color;

	public Race(Pokedex pokedex, GoPokedex goPokedex) {

		setPokedexId(goPokedex.getPokedexId());
		setName(goPokedex.getName());
		setRemarks(goPokedex.getRemarks());

		setPokedex(pokedex);
		setGoPokedex(goPokedex);

//		// タイプ1の色を設定
//		final TypeColorEnum c1 = TypeColorEnum.getTypeColorForJpn(goPokedex.getType1());
//		setType1Color(new Color(c1.getR(), c1.getG(), c1.getB()));
//		// タイプ2の色を設定
//		if (!StringUtils.isEmpty(goPokedex.getType2())) {
//			final TypeColorEnum c2 = TypeColorEnum.getTypeColorForJpn(goPokedex.getType2());
//			setType2Color(new Color(c2.getR(), c2.getG(), c2.getB()));
//		}
//
//		// ポケモンの色（タイプから算出））
//		setColor(new Color(goPokedex));

	}
}
