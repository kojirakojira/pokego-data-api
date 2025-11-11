package jp.brainjuice.pokego.business.service.search.utils.dto.moves;

import jp.brainjuice.pokego.business.constant.LearningPatternEnum;
import jp.brainjuice.pokego.dao.jpa.entity.GoPokedex;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GoPokedexAndMoveInfo {

	private int no;
	private GoPokedex goPokedex;
	private int cp;
	private LearningPatternEnum learningPattern;
	private String learningPatternName;
	private String learningPatternAnnos;
}
