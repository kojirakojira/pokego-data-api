package jp.brainjuice.pokego.web.form.res.elem;

import jp.brainjuice.pokego.business.constant.Type.TypeColorEnum;
import jp.brainjuice.pokego.business.constant.Type.TypeEnum;
import jp.brainjuice.pokego.business.dao.entity.GoPokedex;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Color {

	private int r;
	private int g;
	private int b;

	/**
	 * GoPokedexからColorを生成する。<br>
	 * タイプが2つ存在する場合は、中間の色を生成する。
	 *
	 *
	 * @param goPokedex
	 */
	public Color(GoPokedex goPokedex) {

		final TypeEnum type1 = goPokedex.getType1();
		final TypeEnum type2 = goPokedex.getType2();

		final TypeColorEnum c1 = TypeColorEnum.valueOf(type1);
		if (type2 == null) {
			// タイプ１のみの場合。
			setR(c1.getR());
			setG(c1.getG());
			setB(c1.getB());
		} else {
			// タイプ２がある場合。
			final TypeColorEnum c2 = TypeColorEnum.valueOf(type2);
			setR((c1.getR() + c2.getR()) / 2);
			setG((c1.getG() + c2.getG()) / 2);
			setB((c1.getB() + c2.getB()) / 2);
		}
	}
}
