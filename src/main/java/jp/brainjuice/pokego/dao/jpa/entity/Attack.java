package jp.brainjuice.pokego.dao.jpa.entity;

import jp.brainjuice.pokego.business.constant.Type.TypeEnum;

/**
 * ポケモンの技のインタフェース
 */
public interface Attack {

	String getMoveId();
	String getName();
	String getUniqueId();
	String getMovementNo();
	TypeEnum getType();
}
