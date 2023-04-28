package jp.brainjuice.pokego.business.service.utils.dto.type;

import jp.brainjuice.pokego.business.constant.Type.TypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * こうげきする際のパターンを表現するクラス
 *
 * @author saibabanagchampa
 *
 */
@Data
@AllArgsConstructor
public class AttackPattern {
	TypeEnum atkType;
	TwoTypeKey defType;
}
