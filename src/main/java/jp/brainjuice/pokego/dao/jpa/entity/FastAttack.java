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
 * ノーマル技（Fast Attacks、技1）
 *
 * @author saibabanagchampa
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "fast_attack")
@ToString
public class FastAttack implements Attack, Serializable, Cloneable {

	/** 技ID(タイプコード3桁 + (通常技(1) or スペシャル技(2))1桁 + 連番3桁) */
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

	/** ゲージ増加量（ジム・レイド時）(=energyDelta) */
	@Column(name = "gym_energy_incr_amount", nullable = false)
	private int gymEnergyIncrAmount;

	/** Damage Per Seconds. round(gymPower / totalMs, 2) */
	@Column(nullable = false)
	private double dps;

	/**
	 * Energy Per Second. 1秒あたりのゲージ回復量を指す
	 * round(gymEnergyIncrAmount / totalMs, 2)
	 */
	@Column(nullable = false)
	private double eps;

	/**
	 * 技の発生時間。画面をタップしてからダメージが発生するまでの時間（ミリ秒）
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

	/** ゲージ増加量（PvP時） */
	@Column(name = "pvp_energy_incr_amount", nullable = false)
	private int pvpEnergyIncrAmount;

	/** ターン数（1ターン=0.5秒） */
	@Column(nullable = false)
	private int turns;

	/** Damage Per Turns. round(pvpPower / turns, 2) */
	@Column(nullable = false)
	private double dpt;

	/** Energy Per Turns. round(energy / turns, 2) */
	@Column(nullable = false)
	private double ept;
}
