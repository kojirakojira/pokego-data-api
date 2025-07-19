package jp.brainjuice.pokego.business.service.manage.masterFileAnalyzer.dto.json;

import java.io.Serializable;
import java.util.Map;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class CinematicCombatMove implements Serializable, Cloneable {

	private String uniqueId;
	private String type;
	private float power;
	private String vfxName;
	private float energyDelta;
	private Map<String, Float> buffs;
}
