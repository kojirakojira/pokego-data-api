package jp.brainjuice.pokego.business.dao.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * スペシャル技（Charged Attacks, 技2）
 *
 * @author saibabanagchampa
 *
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ChargedAttack extends Entity {

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

	/** PvP時のダメージ */
	private int pvpPower;

	/** ゲージ増加量（PvP時）。値はマイナス値が正。 */
	private int energyIncrAmount;

	/** Power Per Energy. round(pvpPower / abs(energyIncrAmount), 2) */
	private float dpe;

	/** バフ・デバフの対象1 */
	private String buffTarget1;

	/** バフ・デバフの効果1。現状-3～2で存在する。 */
	private Integer buffEffect1;

	/** バフ・デバフの対象2 */
	private String buffTarget2;

	/** バフ・デバフの効果2。現状-3～2で存在する。 */
	private Integer buffEffect2;

	/** バフ・デバフの発動確率 */
	private Float activationChance;
}
