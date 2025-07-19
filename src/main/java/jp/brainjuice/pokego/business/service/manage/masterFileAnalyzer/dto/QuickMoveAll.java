package jp.brainjuice.pokego.business.service.manage.masterFileAnalyzer.dto;

import jp.brainjuice.pokego.business.service.manage.masterFileAnalyzer.dto.json.Move;
import jp.brainjuice.pokego.business.service.manage.masterFileAnalyzer.dto.json.QuickCombatMove;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuickMoveAll {

	private String movementId;
	private String movementNo; // V9999のやつ
	private Move gymRaid;
	private QuickCombatMove pvp;
}
