package jp.brainjuice.pokego.business.dao.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "cp_multiplier")
public class CpMultiplier {

	/** PL(ポケモンレベル) */
	@Id
	@Column(nullable = false, columnDefinition = "bpchar")
	private String pl;

	/** 倍率 */
	@Column(nullable = false)
	private double multiplier;
}
