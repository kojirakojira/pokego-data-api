package jp.brainjuice.pokego.cache.inmemory.dto;

import jp.brainjuice.pokego.dao.jpa.entity.RaceExceptions;

/**
 * 種族値例外で使用するキー名
 *
 * @see RaceExceptions
 */
public enum RaceEx {

	ATTACK, // 攻撃
	DEFENSE, // 防御
	HP, // HP
	NOT_EXISTS_ORIGIN, // 原作に存在しないポケモンであるか否か
}
