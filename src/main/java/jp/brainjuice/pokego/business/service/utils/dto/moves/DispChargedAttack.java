package jp.brainjuice.pokego.business.service.utils.dto.moves;

import jp.brainjuice.pokego.business.constant.Type.TypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class DispChargedAttack {

	/** 技ID(タイプコード2桁 + (ノーマル技(1) or スペシャル技(2))1桁 + 連番3桁) */
	private String moveId;

	/** 技名（日本語） */
	private String name;

	/** タイプ */
	private TypeEnum type;

	private ChargedGymParam gym;

	private ChargedPvpParam pvp;
}
