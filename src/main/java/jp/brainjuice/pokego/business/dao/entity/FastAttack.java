package jp.brainjuice.pokego.business.dao.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * ノーマル技（Fast Attacks、技1）
 *
 * @author saibabanagchampa
 *
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class FastAttack extends Entity {

	/** 技ID(タイプコード2桁 + (ノーマル技(1) or スペシャル技(2))1桁 + 連番3桁) */
	private String moveId;

	/** 技名（日本語） */
	private String name;

	/** タイプ */
	private String type;

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

	/** PvP時のダメージ */
	private int pvpPower;

	/** ゲージ増加量（PvP時） */
	private int energyIncrAmount;

	/** ターン数（1ターン=0.5秒） */
	private int turns;

	/** Damage Per Turns. round(pvpPower / turns, 2) */
	private float dpt;

	/** Energy Per Turns. round(energy / turns, 2) */
	private float ept;
}
