package jp.brainjuice.pokego.web.search.form.req.pinnacle;

import java.util.List;

import jakarta.validation.constraints.NotNull;

import jp.brainjuice.pokego.business.constant.Type.TypeEnum;
import jp.brainjuice.pokego.business.service.search.pinnacle.GymRaidPinnacleRankService.Order;
import jp.brainjuice.pokego.business.service.search.pinnacle.GymRaidPinnacleRankService.SelectPattern;
import jp.brainjuice.pokego.web.search.form.req.ResearchRequestImpl;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class GymRaidPinnacleRankRequest extends ResearchRequestImpl {

	@NotNull
	private TypeEnum oppType1;

	private TypeEnum oppType2;

	private List<TypeEnum> ownTypes;

	private String weather;

	private SelectPattern megaSelected;

	private SelectPattern shadowSelected;

	private Order order;
	/** ポケモンの重複を除去する場合true */
	private boolean unique;
}
