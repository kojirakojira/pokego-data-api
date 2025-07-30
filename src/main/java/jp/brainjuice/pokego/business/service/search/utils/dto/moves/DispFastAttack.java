package jp.brainjuice.pokego.business.service.search.utils.dto.moves;

import jp.brainjuice.pokego.business.constant.Type.TypeEnum;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@ToString
public class DispFastAttack {

	private int no;

	/** 技ID(タイプコード2桁 + (ノーマル技(1) or スペシャル技(2))1桁 + 連番3桁) */
	private String moveId;

	/** 技名（日本語） */
	private String name;

	/** タイプ */
	private TypeEnum type;

	/** Gym or Raid時のパラメータ */
	private FastGymParam gymRaid;

	/** PvP時のパラメータ */
	private FastPvpParam pvp;

	public DispFastAttack(String moveId, String name, TypeEnum type, FastGymParam gymRaid, FastPvpParam pvp) {
		this.moveId = moveId;
		this.name = name;
		this.type = type;
		this.gymRaid = gymRaid;
		this.pvp = pvp;
	}
}
