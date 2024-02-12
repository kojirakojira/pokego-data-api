package jp.brainjuice.pokego.business.service.utils.dto.evo;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Hierarchy {

	/**
	 * x軸（第何進化のうちの同列のポケモンを表現する。）
	 * ※例：アゲハント:1、ドクケイル:2
	 */
	private int x;
	/**
	 * y軸（第何進化を表現する。）
	 * ※ケムッソ:1、カラサリス:2、アゲハント:3
	 */
	private int y;
	/**
	 * 進化前のポケモンとのx軸方向の距離
	 */
	private int dist;
	/** pokedexId */
	private String id;
	/** 進化前のポケモンのpokedexId */
	private String bid;
	/** 進化条件 */
	private List<String> costs;
	/** ポケモンGOで進化するか */
	private boolean goEvo;

}
