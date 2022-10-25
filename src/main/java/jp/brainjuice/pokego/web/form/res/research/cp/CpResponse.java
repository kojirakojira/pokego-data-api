package jp.brainjuice.pokego.web.form.res.research.cp;

import jp.brainjuice.pokego.business.service.utils.dto.IndividialValue;
import jp.brainjuice.pokego.web.form.res.research.ResearchResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper=false)
@NoArgsConstructor
public class CpResponse extends ResearchResponse {

	private int cp;

	public CpResponse(IndividialValue iv) {
		setMessage("");
		setPokedexId(iv.getGoPokedex().getPokedexId());
		setName(iv.getGoPokedex().getName());
	}
}
