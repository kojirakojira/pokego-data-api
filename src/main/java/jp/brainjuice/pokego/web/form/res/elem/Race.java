package jp.brainjuice.pokego.web.form.res.elem;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import jp.brainjuice.pokego.business.dao.entity.GoPokedex;
import jp.brainjuice.pokego.business.dao.entity.Pokedex;
import jp.brainjuice.pokego.business.service.utils.memory.TypeMap;
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

	public Race(Pokedex pokedex, GoPokedex goPokedex, TypeMap typeMap) {

		setPokedex(pokedex);
		setGoPokedex(goPokedex);

		// タイプ1の色を設定
		final Map<String, Integer> colorMap1 = typeMap.get(goPokedex.getType1());
		setType1Color(new Color(
				colorMap1.get(TypeMap.KeyElem.r.name()),
				colorMap1.get(TypeMap.KeyElem.g.name()),
				colorMap1.get(TypeMap.KeyElem.b.name())));
		// タイプ2の色を設定
		if (!StringUtils.isEmpty(goPokedex.getType2())) {
			final Map<String, Integer> colorMap2 = typeMap.get(goPokedex.getType2());
			setType2Color(new Color(
					colorMap2.get(TypeMap.KeyElem.r.name()),
					colorMap2.get(TypeMap.KeyElem.g.name()),
					colorMap2.get(TypeMap.KeyElem.b.name())));
		}

		// ポケモンの色（タイプから算出））
		setColor(getColor(goPokedex, typeMap));

	}

	/**
	 * ポケモンのタイプの色を取得します。（タイプ２が存在する場合は中間の色を取得する。）
	 *
	 * @return
	 */
	private Color getColor(GoPokedex goPokedex, TypeMap typeMap) {

		Color color = null;

		final String type1 = goPokedex.getType1();
		final String type2 = goPokedex.getType2();
		if (StringUtils.isEmpty(type2)) {
			// タイプ１のみの場合。
			final Map<String, Integer> colorMap = typeMap.get(type1);
			color = new Color(
					colorMap.get(TypeMap.KeyElem.r.name()),
					colorMap.get(TypeMap.KeyElem.g.name()),
					colorMap.get(TypeMap.KeyElem.b.name()));
		} else {
			// タイプ２がある場合。
			Map<String, Integer> colorMap1 = typeMap.get(type1);
			Map<String, Integer> colorMap2 = typeMap.get(type2);
			color = new Color(
					(colorMap1.get(TypeMap.KeyElem.r.name()) + colorMap2.get(TypeMap.KeyElem.r.name())) / 2,
					(colorMap1.get(TypeMap.KeyElem.g.name()) + colorMap2.get(TypeMap.KeyElem.g.name())) / 2,
					(colorMap1.get(TypeMap.KeyElem.b.name()) + colorMap2.get(TypeMap.KeyElem.b.name())) / 2);
		}

		return color;
	}
}
