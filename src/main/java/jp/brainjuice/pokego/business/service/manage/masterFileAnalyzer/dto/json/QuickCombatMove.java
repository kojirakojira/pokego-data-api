package jp.brainjuice.pokego.business.service.manage.masterFileAnalyzer.dto.json;

import java.io.Serializable;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class QuickCombatMove implements Serializable, Cloneable {

	private String uniqueId;
	private String type;
	private double power;
	private String vfxName;
	private int durationTurns;
	private int energyDelta;
}
