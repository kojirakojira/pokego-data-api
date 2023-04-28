package jp.brainjuice.pokego.business.service.utils.dto.type;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 1vs1で戦う際のタイプのパターンを表現するクラス
 *
 * @author saibabanagchampa
 *
 */
@Data
@AllArgsConstructor
public class BattlePattern {
	TwoTypeKey ownType;
	TwoTypeKey oppType;
}
