package jp.brainjuice.pokego.dao.jpa.entity;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import jp.brainjuice.pokego.business.constant.AttackAnnotationTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * ポケモンごとの通常技
 *
 * @author saibabanagchampa
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "attack_additional_info")
@ToString
//@IdClass(PokemonAttackPk.class)
public class AttackAdditionalInfo implements Serializable, Cloneable {

	/** 図鑑ID */
	@Id
	@Column(name = "move_id", nullable = false, columnDefinition = "bpchar")
	private String moveId;

	/** 技ID */
	@Id
	@Column(name = "pokedex_id", nullable = false, columnDefinition = "bpchar")
	private String pokedexId;

	@Id
	@Enumerated(EnumType.STRING)
	@Column(name = "annotation_type", nullable = false, columnDefinition = "bpchar")
	private AttackAnnotationTypeEnum annotationType;

	/** 追加の説明文 */
	private String text;

}
