package jp.brainjuice.pokego.web.form.res.elem;

import org.apache.commons.lang3.StringUtils;

import jp.brainjuice.pokego.business.constant.Type.TypeColorEnum;
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

	private Pokedex pokedex;
	private GoPokedex goPokedex;
	private Color color;
	private Color type1Color;
	private Color type2Color;

	public Race(Pokedex pokedex, GoPokedex goPokedex) {

		setPokedex(pokedex);
		setGoPokedex(goPokedex);

		// タイプ1の色を設定
		final TypeColorEnum c1 = TypeColorEnum.getTypeColorForJpn(goPokedex.getType1());
		setType1Color(new Color(c1.getR(), c1.getG(), c1.getB()));
		// タイプ2の色を設定
		if (!StringUtils.isEmpty(goPokedex.getType2())) {
			final TypeColorEnum c2 = TypeColorEnum.getTypeColorForJpn(goPokedex.getType2());
			setType2Color(new Color(c2.getR(), c2.getG(), c2.getB()));
		}

		// ポケモンの色（タイプから算出））
		setColor(getColor(goPokedex));

	}

	/**
	 * ポケモンのタイプの色を取得します。（タイプ２が存在する場合は中間の色を取得する。）
	 *
	 * @return
	 */
	private Color getColor(GoPokedex goPokedex) {

		Color color = null;

		final String type1 = goPokedex.getType1();
		final String type2 = goPokedex.getType2();

		final TypeColorEnum c1 = TypeColorEnum.getTypeColorForJpn(type1);
		if (StringUtils.isEmpty(type2)) {
			// タイプ１のみの場合。
			color = new Color(c1.getR(), c1.getG(), c1.getB());
		} else {
			// タイプ２がある場合。
			final TypeColorEnum c2 = TypeColorEnum.getTypeColorForJpn(type2);
			color = new Color(
					(c1.getR() + c2.getR()) / 2,
					(c1.getG() + c2.getG()) / 2,
					(c1.getB() + c2.getB()) / 2);
		}

		return color;
	}
}
