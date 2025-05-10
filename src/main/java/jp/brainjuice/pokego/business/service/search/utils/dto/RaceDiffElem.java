package jp.brainjuice.pokego.business.service.search.utils.dto;

import jp.brainjuice.pokego.web.search.form.res.elem.Race;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RaceDiffElem {

	private Race race;
	private int cp;
}
