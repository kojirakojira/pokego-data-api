package jp.brainjuice.pokego.business.service.search.utils.dto;

import jp.brainjuice.pokego.dao.jpa.entity.GoPokedex;
import jp.brainjuice.pokego.dao.jpa.entity.Pokedex;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GppAndCp {
	private int no;
	private GoPokedex goPokedex;
	private Pokedex pokedex;
	private int cp;
}
