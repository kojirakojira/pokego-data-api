package jp.brainjuice.pokego.web.form.res;

import jp.brainjuice.pokego.business.service.utils.dto.PokemonFilterResult;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class FilterAllResponse extends Response {

	PokemonFilterResult pfr;

}
