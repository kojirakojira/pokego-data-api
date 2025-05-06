package jp.brainjuice.pokego.web.search.form.res.moves;

import java.util.List;

import jp.brainjuice.pokego.business.service.search.utils.dto.moves.DispChargedAttack;
import jp.brainjuice.pokego.business.service.search.utils.dto.moves.DispFastAttack;
import jp.brainjuice.pokego.web.search.form.res.Response;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class MoveListResponse extends Response {

	private List<DispFastAttack> faList;
	private List<DispChargedAttack> caList;

}
