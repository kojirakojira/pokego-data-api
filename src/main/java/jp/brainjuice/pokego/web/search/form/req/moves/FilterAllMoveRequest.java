package jp.brainjuice.pokego.web.search.form.req.moves;

import java.util.List;

import jp.brainjuice.pokego.business.constant.Type.TypeEnum;
import jp.brainjuice.pokego.business.service.search.moves.FilterAllMoveService.MoveDispTypeEnum;
import lombok.Data;

@Data
public class FilterAllMoveRequest {

	private List<TypeEnum> types;
	private MoveDispTypeEnum moveDispType;
}
