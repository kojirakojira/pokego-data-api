package jp.brainjuice.pokego.business.service.utils.dto.moves;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ChargedGymParam {

	/**
	 * ジム、レイド時のダメージ
	 * ※実際に相手に与えるダメージではないため注意
	 */
	private int gymPower;

	/** Damage Per Seconds. round(gymPower / totalTime, 2) */
	private float dps;

	/**
	 * 技の発生時間。画面をタップしてからダメージが発生するまでの時間。
	 */
	private float damagedTime;

	/**
	 * 全体時間。durationともいう。画面をタップしてから硬直が終了するまで（≒次の行動ができるようになるまで）の時間
	 */
	private float totalTime;

	/**
	 * ゲージ本数。1 or 2 or 3。（正確にはゲージが何分割されているかを表す数値。）
	 */
	private int energyBar;

}
