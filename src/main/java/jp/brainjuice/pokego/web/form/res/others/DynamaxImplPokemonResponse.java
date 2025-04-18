package jp.brainjuice.pokego.web.form.res.others;

import java.util.List;

import jp.brainjuice.pokego.business.dao.entity.GoPokedex;
import jp.brainjuice.pokego.web.form.res.Response;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class DynamaxImplPokemonResponse extends Response {

	private List<GoPokedex> dynamaxList;
	private String lastUpdated;
}
