package jp.brainjuice.pokego.web.search.form.res.moves;

import java.util.List;

import jp.brainjuice.pokego.business.service.search.utils.dto.moves.MoveCombination;
import jp.brainjuice.pokego.dao.jpa.entity.GoPokedex;
import jp.brainjuice.pokego.web.search.form.res.ResearchResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class GymRaidPokeMoveCombiResponse extends ResearchResponse {

	private GoPokedex goPokedex;
	private GoPokedex preMegaGp;
	private List<MoveCombination> moveCombiList;
}
