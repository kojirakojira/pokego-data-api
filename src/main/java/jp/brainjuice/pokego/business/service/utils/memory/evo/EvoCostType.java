package jp.brainjuice.pokego.business.service.utils.memory.evo;

import lombok.Getter;

public enum EvoCostType {
	/** 進化に必要なアメの個数 */
	candy("アメ"),
	/** 進化に必要な進化アイテム */
	evolutionItems("進化アイテム"),
	/** 進化に必要な相棒としてのアクション */
	buddy("相棒"),
	/** 特殊な行動、特殊な条件 */
	specialAction("特殊な条件"),
	/** ルアーモジュールを使用した進化条件 */
	lureModules("ルアーモジュール"),
	/** 進化に必要な交換の条件 */
	tradeEvolution("交換");

	EvoCostType(String jpn) {
		this.jpn = jpn;
	}

	@Getter
	private final String jpn;
}
