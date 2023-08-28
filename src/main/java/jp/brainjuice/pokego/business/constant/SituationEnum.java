package jp.brainjuice.pokego.business.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * ポケモンを捕まえるときのシチュエーション
 *
 * @author saibabanagchampa
 *
 */
@AllArgsConstructor
public enum SituationEnum implements ConstantEnumInterface {

	wild("野生"),
	frTask("フィールドリサーチタスクのクリアボーナス"),
	raid("レイドバトル勝利ボーナス"),
	raidShadow("レイドバトル勝利ボーナス（シャドウ）"),
	egg("タマゴ孵化"),
	rocket("ロケット団勝利ボーナス"),
	rocketSakaki("ロケット団勝利ボーナス（サカキ戦）"),
	non("絞り込みなし"),
	;

	@Getter
	private final String jpn;

	public static SituationEnum getJpn(String str) {
		for(SituationEnum v : values()) {
			if(v.getJpn().equals(str)) {
				return v;
			}
		}
		return null;
	}

}
