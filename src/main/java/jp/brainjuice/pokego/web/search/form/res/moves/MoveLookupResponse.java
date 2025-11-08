package jp.brainjuice.pokego.web.search.form.res.moves;

import jp.brainjuice.pokego.business.service.search.utils.dto.moves.ChargedAttackDetails;
import jp.brainjuice.pokego.business.service.search.utils.dto.moves.FastAttackDetails;
import jp.brainjuice.pokego.business.service.search.utils.dto.moves.MoveSearchResult;
import jp.brainjuice.pokego.web.Response;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class MoveLookupResponse extends Response {

	private MoveSearchResult moveSearchResult;

	private String moveId;
	private String name;

	private FastAttackDetails fastAttackDetails;

	private ChargedAttackDetails chargedAttackDetails;
}
