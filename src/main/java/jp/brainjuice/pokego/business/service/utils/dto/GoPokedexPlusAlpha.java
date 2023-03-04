package jp.brainjuice.pokego.business.service.utils.dto;

import jp.brainjuice.pokego.business.dao.entity.GoPokedex;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GoPokedexPlusAlpha {

	private GoPokedex goPokedex;
	private int cp;
}
