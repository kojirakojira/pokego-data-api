package jp.brainjuice.pokego.business.service.utils.dto.moves;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class FastGymParam {

	/**
	 * ジム、レイド時のダメージ
	 * ※実際に相手に与えるダメージではないため注意
	 */
	private int gymPower;

	/** Power Per Seconds. round(gymPower / totalTime, 2) */
	private float dps;

	/**
	 * Energy Per Second. 1秒あたりのゲージ回復量を指す。
	 * ただ、ゲージ回復量の情報は一般的に出回っておらず、EPSという数値が独り歩きしている模様。
	 */
	private float eps;

	/**
	 * 技の発生時間。画面をタップしてからダメージが発生するまでの時間。
	 */
	private float damagedTime;

	/**
	 * 全体時間。durationともいう。画面をタップしてから硬直が終了するまで（≒次の行動ができるようになるまで）の時間
	 */
	private float totalTime;
}
