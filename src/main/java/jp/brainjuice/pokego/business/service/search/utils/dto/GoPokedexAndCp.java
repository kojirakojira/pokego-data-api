package jp.brainjuice.pokego.business.service.search.utils.dto;

import jp.brainjuice.pokego.dao.jpa.entity.GoPokedex;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GoPokedexAndCp {

	private GoPokedex goPokedex;
	private int cp;
}
