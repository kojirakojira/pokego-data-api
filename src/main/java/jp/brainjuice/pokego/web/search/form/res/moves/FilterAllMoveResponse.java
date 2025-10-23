package jp.brainjuice.pokego.web.search.form.res.moves;

import java.util.List;

import jp.brainjuice.pokego.business.service.search.utils.dto.moves.DispChargedAttack;
import jp.brainjuice.pokego.business.service.search.utils.dto.moves.DispFastAttack;
import jp.brainjuice.pokego.web.search.form.res.Response;
import jp.brainjuice.pokego.web.search.form.res.elem.DispFilterParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class FilterAllMoveResponse extends Response {

	/** 絞り込みをした検索値 */
	private List<DispFilterParam> filteredItems;

	private List<DispFastAttack> faList;
	private List<DispChargedAttack> caList;

	private boolean shouldDispFaList;
	private boolean shouldDispCaList;

}
