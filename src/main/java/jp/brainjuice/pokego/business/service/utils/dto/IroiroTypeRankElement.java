package jp.brainjuice.pokego.business.service.utils.dto;

import java.util.Arrays;
import java.util.List;

import jp.brainjuice.pokego.business.service.utils.dto.type.TwoTypeKey;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 色々タイプランキングにおけるレスポンスに使用する。<br>
 * ランキング表示に使用するクラス。
 *
 * @author saibabanagchampa
 *
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class IroiroTypeRankElement {

	private TwoTypeKey twoTypeKey;
	private int rank;
	private List<String> msgs;

	public IroiroTypeRankElement(TwoTypeKey twoTypeKey, int rank, String msg) {
		setTwoTypeKey(twoTypeKey);
		setRank(rank);
		setMsgs(Arrays.asList(msg));
	}
}
