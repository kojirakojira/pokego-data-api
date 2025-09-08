package jp.brainjuice.pokego.business.service.search.utils.dto.moves;

import jp.brainjuice.pokego.business.constant.LearningPatternEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class DispPokemonFastAttack extends DispFastAttack {

	private LearningPatternEnum learningPattern;
	private String learningPatternName;
	private String additionalInfo;

	/**
	 * 表示用のFastAttackの情報から設定可能な項目だけ設定するコンストラクタ
	 * @param dispFastAttack
	 */
	public DispPokemonFastAttack(DispFastAttack dispFastAttack) {
		super(
				dispFastAttack.getMoveId(),
				dispFastAttack.getName(),
				dispFastAttack.getType(),
				dispFastAttack.getGymRaid(),
				dispFastAttack.getPvp());
	}
}
