package jp.brainjuice.pokego.business.dao.entity;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@ToString
@Table(name = "evolution")
@IdClass(EvolutionPk.class)
public class Evolution implements Serializable {

	/** 図鑑ID */
	@Id
	@Column(name = "pokedex_id", nullable = false, columnDefinition = "bpchar")
	private String pokedexId;

	/** 進化前の図鑑ID */
	@Id
	@Column(name = "before_pokedex_id", nullable = false, columnDefinition = "bpchar")
	private String beforePokedexId;

	/** 進化に必要なアメの個数 */
	@Column(nullable = false)
	private int candy;

	/** 進化に必要な進化アイテム */
	@Column(name = "evolution_items", length = 30)
	private String evolutionItems;

	/** 進化に必要な相棒としてのアクション */
	@Column(length = 100)
	private String buddy;

	/** 特殊な行動、特殊な条件 */
	@Column(name = "special_action", length = 100)
	private String specialAction;

	/** ルアーモジュールを使用した進化条件 */
	@Column(name = "lure_modules", length = 100)
	private String lureModules;

	/** 進化に必要な交換の条件 */
	@Column(name = "trade_evolution", length = 100)
	private String tradeEvolution;
	/**
	 * 進化方法の注釈
	 * ※進化ツリー全体に対して説明したい条件がある場合に使用する。（ランダムの場合等）
	 */
	@Column(name = "evol_annotations", length = 256)
	private String evolAnnotations;

	/** ポケモンGOで進化するかどうか */
	@Column(name = "can_go_evol", nullable = false)
	private boolean canGoEvol;

}
