package jp.brainjuice.pokego.business.service.manage.masterFileAnalyzer.dto.json;

import java.util.List;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class ReplacementMove {

	private List<String> existingMoves;
	private List<String> replacementMoves;
}
