package jp.brainjuice.pokego.business.service.manage.masterFileAnalyzer.dto;

import jp.brainjuice.pokego.business.constant.Type.TypeEnum;
import jp.brainjuice.pokego.business.service.manage.masterFileAnalyzer.dto.json.CinematicCombatMove;
import jp.brainjuice.pokego.business.service.manage.masterFileAnalyzer.dto.json.Move;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class CinematicMoveAll {

	private String movementId;
	private String movementNo;
	private TypeEnum type;
	private Move gymRaid;
	private CinematicCombatMove pvp;
}
