package jp.brainjuice.pokego.dao.jpa.entity;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;

import jp.brainjuice.pokego.business.constant.AttackAnnotationTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * ポケモンごとの技の主キー
 * 通常技、スペシャル技で併用している。
 */
@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class AttackAdditionalInfoPk implements Serializable {

	/** 図鑑ID */
	@Id
	@Column(name = "pokedex_id", nullable = false, columnDefinition = "bpchar")
	private String pokedexId;

	/** 技ID */
	@Id
	@Column(name = "move_id", nullable = false, columnDefinition = "bpchar")
	private String moveId;

	@Id
	@Enumerated(EnumType.STRING)
	@Column(name = "annotation_type", nullable = false, columnDefinition = "bpchar")
	private AttackAnnotationTypeEnum annotationType;

	/**
	 * (非 Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return Objects.hash(pokedexId, moveId, annotationType);
	}

	/**
	 * (非 Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null || getClass() != obj.getClass()) return false;
		AttackAdditionalInfoPk aaiPk = (AttackAdditionalInfoPk) obj;
		return Objects.equals(pokedexId, aaiPk.getPokedexId())
				&& Objects.equals(moveId, aaiPk.getMoveId())
				&& annotationType == aaiPk.getAnnotationType();
	}
}
