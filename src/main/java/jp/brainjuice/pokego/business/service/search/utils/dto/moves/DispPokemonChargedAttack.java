package jp.brainjuice.pokego.business.service.search.utils.dto.moves;

import jp.brainjuice.pokego.business.constant.LearningPatternEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class DispPokemonChargedAttack extends DispChargedAttack {

	private LearningPatternEnum learningPattern;
	private String learningPatternName;
	private String learningPatternAnnos;

	/**
	 * 表示用のChargedAttackの情報から設定可能な項目だけ設定するコンストラクタ
	 * @param dispChargedAttack
	 */
	public DispPokemonChargedAttack(DispChargedAttack dispChargedAttack) {
		super(
				dispChargedAttack.getMoveId(),
				dispChargedAttack.getName(),
				dispChargedAttack.getType(),
				dispChargedAttack.getGymRaid(),
				dispChargedAttack.getPvp());
	}
}
