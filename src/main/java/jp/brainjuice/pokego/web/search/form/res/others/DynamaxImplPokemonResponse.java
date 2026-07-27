package jp.brainjuice.pokego.web.search.form.res.others;

import java.util.List;

import jp.brainjuice.pokego.dao.jpa.entity.GoPokedex;
import jp.brainjuice.pokego.web.Response;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class DynamaxImplPokemonResponse extends Response {

	private List<GoPokedex> dynamaxList;
	private String lastUpdated;
}
