package jp.brainjuice.pokego.business.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * ポケモンの技のカテゴリーを表す
 */
@AllArgsConstructor
public enum LearningPatternEnum implements ConstantEnumInterface {

	normal("-"),
	elite("レガシー"),
	purified("リトレーン"),
	shadow("シャドウ"),
	formChange("フォルムチェンジ"),
	other("その他") //今のところガリョウテンセイのみのはず
	;

	@Getter
	private final String jpn;
}
