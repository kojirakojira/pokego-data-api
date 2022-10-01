package jp.brainjuice.pokego.business.service.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jp.brainjuice.pokego.business.dao.entity.GoPokedex;
import jp.brainjuice.pokego.business.dao.entity.Pokedex;
import jp.brainjuice.pokego.business.service.utils.memory.IdentifierPokemonList;

@Component
public class PokemonUtils {

	private IdentifierPokemonList identifierPokemonList;

	private PokemonGoUtils pokemonGoUtils;

	public PokemonUtils (PokemonGoUtils pokemonGoUtils) {
		this.pokemonGoUtils = pokemonGoUtils;
	}

	@Autowired
	public PokemonUtils(IdentifierPokemonList identifierPokemonList,
			PokemonGoUtils pokemonGoUtils) {
		this.identifierPokemonList = identifierPokemonList;
		this.pokemonGoUtils = pokemonGoUtils;
	}

	/**
	 * GOステータスを取得します。
	 *
	 * @param pokedex
	 * @return
	 */
	public GoPokedex getGoPokedex(Pokedex pokedex) {

		int attack = convGoAttack(
				pokedex.getAttack(),
				pokedex.getSpecialAttack(),
				pokedex.getSpeed(),
				pokedex.getPokedexId());

		int defense = convGoDefense(
				pokedex.getDefense(),
				pokedex.getSpecialDefense(),
				pokedex.getSpeed(),
				pokedex.getPokedexId());

		int hp = convGoHp(pokedex.getHp(), pokedex.getPokedexId());

		GoPokedex goPokedex = new GoPokedex();
		goPokedex.setPokedexId(pokedex.getPokedexId());
		goPokedex.setName(pokedex.getName());
		goPokedex.setAttack(attack);
		goPokedex.setDefense(defense);
		goPokedex.setHp(hp);
		goPokedex.setRemarks(pokedex.getRemarks());

		return goPokedex;
	}

	/**
	 * 原作→Go HP変換
	 *
	 * @param hp
	 * @return
	 */
	public int convGoHp(int hp) {

		double baseHp = baseHp(hp);

		return (int) Math.floor(baseHp);
	}

	/**
	 * 原作→Go HP変換（強キャラのCP補正あり）
	 *
	 * @param hp
	 * @return
	 */
	public int convGoHp(int hp, String name) {

		double baseHp = baseHp(hp);

		baseHp = isIdentifierPokemon(name) ? baseHp * 0.91 : baseHp;

		return (int) Math.floor(baseHp);
	}

	/**
	 * 原作のHPからGoの基礎HPを取得します。
	 *
	 * @param hp
	 * @return
	 */
	private double baseHp(int hp) {

		// 1.75 * HP + 50（小数点以下切り捨て）
		double baseHp = 1.75 * hp + 50;

		return baseHp;
	}

	/**
	 * 原作→Go 攻撃変換
	 *
	 * @param attack
	 * @param spAttack
	 * @param speed
	 * @return
	 */
	public int convGoAttack(int attack, int spAttack, int speed) {

		double baseAttack = baseAttack(attack, spAttack, speed);

		return (int) Math.round(baseAttack);
	}

	/**
	 * 原作→Go 攻撃変換（教キャラのCP補正あり）
	 *
	 * @param attack
	 * @param spAttack
	 * @param speed
	 * @param name
	 * @return
	 */
	public int convGoAttack(int attack, int spAttack, int speed, String name) {

		double baseAttack = baseAttack(attack, spAttack, speed);

		baseAttack = isIdentifierPokemon(name) ? baseAttack * 0.91 : baseAttack;

		return (int) Math.round(baseAttack);
	}

	/**
	 * 原作のこうげきからGoの基礎こうげきを取得します。
	 *
	 * @param attack
	 * @param spAttack
	 * @param speed
	 * @return
	 */
	private double baseAttack(int attack, int spAttack, int speed) {

		int higher = attack > spAttack ? attack : spAttack;
		int lower = attack > spAttack ? spAttack : attack;

		// 2 × (0.875 × [高い方] + 0.125 × [低い方])を四捨五入
		int scaledAttack = (int) Math.round(2 * (0.875 * higher + 0.125 * lower));

		// ScaledAttack × SpeedMod
		double baseAttack = scaledAttack * speedMod(speed);

		return baseAttack;
	}

	/**
	 * 原作→Go 防御変換
	 *
	 * @param defense
	 * @param spDefense
	 * @param speed
	 * @return
	 */
	public int convGoDefense(int defense, int spDefense, int speed) {

		double baseDefense = baseDefense(defense, spDefense, speed);

		return (int) Math.round(baseDefense);
	}

	/**
	 * 原作→Go 防御変換（教キャラのCP補正あり）
	 *
	 * @param defense
	 * @param spDefense
	 * @param speed
	 * @param name
	 * @return
	 */
	public int convGoDefense(int defense, int spDefense, int speed, String name) {

		double baseDefense = baseDefense(defense, spDefense, speed);

		baseDefense = isIdentifierPokemon(name) ? baseDefense * 0.91 : baseDefense;

		return (int) Math.round(baseDefense);
	}

	/**
	 * 原作のぼうぎょからGoの基礎ぼうぎょを取得します。
	 *
	 * @param defense
	 * @param spDefense
	 * @return
	 */
	private double baseDefense(int defense, int spDefense, int speed) {

		int higher = defense > spDefense ? defense : spDefense;
		int lower = defense > spDefense ? spDefense : defense;

		// 2 × (0.625 × [高い方] + 0.375 × [低い方])を四捨五入
		int scaledDefense = (int) Math.round(2 * (0.625 * higher + 0.375 * lower));

		// ScaledDefense × SpeedMod
		double baseDefense = scaledDefense * speedMod(speed);

		return baseDefense;
	}

	/**
	 * 原作→GO変換において、1/1.1(0.91)倍になるポケモンかどうかを判定します。
	 *
	 * @param pokedexId
	 * @return
	 */
	public boolean isIdentifierPokemon(String pokedexId) {

		return identifierPokemonList.contains(pokedexId);

	}

	/**
	 * 素早さ補正値
	 *
	 * @param speed
	 * @return
	 */
	private double speedMod(int speed) {
		double speedD =Integer.valueOf(speed).doubleValue();
		return 1 + (speedD - 75) / 500;
	}

	/**
	 * 原作の種族値からポケモンGOのCPを求めます。<br>
	 *
	 * @param pokedex
	 * @param pl
	 * @return
	 */
	public int culcCpFromMain(Pokedex pokedex, String pl) {
		return pokemonGoUtils.culcCp(
				convGoAttack(pokedex.getAttack(), pokedex.getSpecialAttack(), pokedex.getSpeed(), pokedex.getPokedexId()),
				convGoDefense(pokedex.getDefense(), pokedex.getSpecialDefense(), pokedex.getSpeed(), pokedex.getPokedexId()),
				convGoHp(pokedex.getHp(), pokedex.getPokedexId()),
				pl);
	}

	/**
	 * 原作の種族値からポケモンGOのPL40の場合のCPを求めます。<br>
	 * これは、ポケモンGOの基礎となる種族値です。<br>
	 * （このCPが4000を超えるかどうかが、個体値補正の基準になります。）
	 *
	 * @param attack
	 * @param defense
	 * @param hp
	 * @param cpMultiplierMap
	 * @return
	 */
	public int culcBaseCpFromMain(Pokedex pokedex) {

		return pokemonGoUtils.culcBaseCp(
				convGoAttack(pokedex.getAttack(), pokedex.getSpecialAttack(), pokedex.getSpeed()),
				convGoDefense(pokedex.getDefense(), pokedex.getSpecialDefense(), pokedex.getSpeed()),
				convGoHp(pokedex.getHp()));
	}

	/**
	 * 原作の種族値からポケモンGOの最大CPを求めます。<br>
	 *
	 * @param pokedex
	 * @param pl
	 * @return
	 */
	public int culcMaxIvCpFromMain(Pokedex pokedex, String pl) {
		return pokemonGoUtils.culcMaxIvCp(
				convGoAttack(pokedex.getAttack(), pokedex.getSpecialAttack(), pokedex.getSpeed(), pokedex.getPokedexId()),
				convGoDefense(pokedex.getDefense(), pokedex.getSpecialDefense(), pokedex.getSpeed(), pokedex.getPokedexId()),
				convGoHp(pokedex.getHp(), pokedex.getPokedexId()),
				pl);
	}

	/**
	 * 原作の種族値からポケモンGOの最大CPを求めます。<br>
	 *
	 * @param pokedex
	 * @param pl
	 * @return
	 */
	public int culcMaxCpFromMain(Pokedex pokedex) {
		return pokemonGoUtils.culcMaxCp(
				convGoAttack(pokedex.getAttack(), pokedex.getSpecialAttack(), pokedex.getSpeed(), pokedex.getPokedexId()),
				convGoDefense(pokedex.getDefense(), pokedex.getSpecialDefense(), pokedex.getSpeed(), pokedex.getPokedexId()),
				convGoHp(pokedex.getHp(), pokedex.getPokedexId()));
	}
}
