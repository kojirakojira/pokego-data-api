package jp.brainjuice.pokego.business.service.utils.dto;

import java.util.List;

import jp.brainjuice.pokego.business.service.utils.dto.type.TwoTypeKey;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * XType検索におけるレスポンスに使用する。<br>
 * ランキング表示に使用するクラス。
 *
 * @author saibabanagchampa
 *
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class XTypeElement {

	private TwoTypeKey twoTypeKey;
	private int rank;
	private List<String> atkMsgs;
	private List<String> defMsgs;
}
