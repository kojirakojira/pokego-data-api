package jp.brainjuice.pokego.dao.jpa.entity;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import jp.brainjuice.pokego.business.constant.Type.TypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * スペシャル技（Charged Attacks, 技2）
 *
 * @author saibabanagchampa
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "charged_attack")
@ToString
public class ChargedAttack implements Serializable, Cloneable {

	/** 技ID(タイプコード2桁 + (ノーマル技(1) or スペシャル技(2))1桁 + 連番3桁) */
	@Id
	@Column(name = "move_id", nullable = false, columnDefinition = "bpchar")
	private String moveId;

	/** 技名（日本語） */
	@Column(nullable = false, length = 40)
	private String name;

	/** マスタデータ上のId */
	@Column(name = "unique_id", nullable = false, columnDefinition = "bpchar")
	private String uniqueId;

	/** マスタデータ上のNo */
	@Column(name = "movement_no", nullable = false, columnDefinition = "bpchar")
	private String movementNo;

	/** タイプ */
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 10)
	private TypeEnum type;

	/**
	 * ジム、レイド時のダメージ
	 * ※実際に相手に与えるダメージではないため注意
	 */
	@Column(name = "gym_power", nullable = false)
	private double gymPower;

	/** ゲージ増加量（ジム・レイド時） */
	@Column(name = "gym_energy_incr_amount", nullable = false)
	private int gymEnergyIncrAmount;

	/** Damage Per Seconds. round(gymPower / totalTime, 2) */
	@Column(nullable = false)
	private double dps;

	/**
	 * ゲージ本数。1 or 2 or 3。（正確にはゲージが何分割されているかを表す数値。）
	 */
	@Column(name = "energy_bar", nullable = false)
	private int energyBar;

	/**
	 * 技の発生時間。画面をタップしてからダメージが発生するまでの時間。
	 */
	@Column(name = "damage_ms", nullable = false)
	private int damageMs;

	/**
	 * 全体時間。durationともいう。画面をタップしてから硬直が終了するまで（≒次の行動ができるようになるまで）の時間
	 */
	@Column(name = "total_ms", nullable = false)
	private int totalMs;

	/** PvP時のダメージ */
	@Column(name = "pvp_power", nullable = false)
	private double pvpPower;

	/** ゲージ増加量（PvP時）。値はマイナス値になる */
	@Column(name = "pvp_energy_incr_amount", nullable = false)
	private int pvpEnergyIncrAmount;

	/** Power Per Energy. round(pvpPower / abs(energyIncrAmount), 2) */
	@Column(nullable = false)
	private double dpe;

	/** 自身のこうげきに対するバフ・デバフ */
	@Column(name = "own_attack_buff", nullable = false)
	private int ownAttackBuff;

	/** 自身のぼうぎょに対するバフ・デバフ */
	@Column(name = "own_defense_buff", nullable = false)
	private int ownDefenseBuff;

	/** 相手のこうげきに対するバフ・デバフ */
	@Column(name = "opp_attack_buff", nullable = false)
	private int oppAttackBuff;

	/** 相手のぼうぎょにたいするバフ・デバフ */
	@Column(name = "opp_defense_buff", nullable = false)
	private int oppDefenseBuff;

	/** バフ・デバフの発動確率 */
	@Column(name = "activation_chance", nullable = false)
	private double activationChance;
}
