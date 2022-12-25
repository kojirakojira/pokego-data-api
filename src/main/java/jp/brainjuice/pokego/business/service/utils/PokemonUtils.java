package jp.brainjuice.pokego.business.service.utils;

import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import jp.brainjuice.pokego.business.dao.entity.GoPokedex;
import jp.brainjuice.pokego.business.dao.entity.Pokedex;
import jp.brainjuice.pokego.business.service.utils.memory.TooStrongPokemonList;
import jp.brainjuice.pokego.utils.exception.PokemonDataInitException;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class PokemonUtils {

	private TooStrongPokemonList tooStrongPokemonList;

	private PokemonGoUtils pokemonGoUtils;

	private Map<String, Object> raceExMap;

	public PokemonUtils (PokemonGoUtils pokemonGoUtils) {
		this.pokemonGoUtils = pokemonGoUtils;

		init();
	}

	@Autowired
	public PokemonUtils(TooStrongPokemonList tooStrongPokemonList,
			PokemonGoUtils pokemonGoUtils) {
		this.tooStrongPokemonList = tooStrongPokemonList;
		this.pokemonGoUtils = pokemonGoUtils;
	}

	/**
	 * race-exceptions.ymlで使用するKey名
	 *
	 * @author saibabanagchampa
	 *
	 */
	private enum RaceEx {

		ATTACK, // 攻撃
		DEFENSE, // 防御
		HP, // HP

		fixed, // 固定値
	}

	/**
	 * race-exceptions.ymlを読み取る。
	 *
	 * @throws PokemonDataInitException
	 */
	@SuppressWarnings("unchecked")
	@PostConstruct
	public void init() {

		raceExMap = new HashMap<String, Object>();
		DefaultResourceLoader resourceLoader;
		InputStreamReader reader;
		try {
			resourceLoader = new DefaultResourceLoader();
			Resource resource = resourceLoader.getResource("classpath:config/race-exceptions.yml");
			reader = new InputStreamReader(resource.getInputStream());

			Yaml yaml = new Yaml();
			raceExMap.putAll(yaml.loadAs(reader, Map.class));

			//
			if (raceExMap.get(RaceEx.HP.name()) == null) { raceExMap.put(RaceEx.HP.name(), new HashMap<>()); }
			if (raceExMap.get(RaceEx.ATTACK.name()) == null) { raceExMap.put(RaceEx.ATTACK.name(), new HashMap<>()); }
			if (raceExMap.get(RaceEx.DEFENSE.name()) == null) { raceExMap.put(RaceEx.DEFENSE.name(), new HashMap<>()); }
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
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
				pokedex.getPokedexId(),
				true);

		int defense = convGoDefense(
				pokedex.getDefense(),
				pokedex.getSpecialDefense(),
				pokedex.getSpeed(),
				pokedex.getPokedexId(),
				true);

		int hp = convGoHp(pokedex.getHp(), pokedex.getPokedexId(), true);

		GoPokedex goPokedex = new GoPokedex();
		goPokedex.setPokedexId(pokedex.getPokedexId());
		goPokedex.setName(pokedex.getName());
		goPokedex.setAttack(attack);
		goPokedex.setDefense(defense);
		goPokedex.setHp(hp);
		goPokedex.setRemarks(pokedex.getRemarks());
		goPokedex.setType1(pokedex.getType1());
		goPokedex.setType2(pokedex.getType2());
		goPokedex.setGen(pokedex.getGen());
		goPokedex.setImplFlg(pokedex.isImplFlg());

		return goPokedex;
	}

	/**
	 * 原作→Go HP変換
	 *
	 * @param hp
	 * @param pokedexId
	 * @param correctFlg 強キャラ補正フラグ
	 * @return
	 */
	public int convGoHp(int hp, String pokedexId, boolean correctFlg) {

		// 例外の固定値が存在する場合はその値を返却する。
		@SuppressWarnings("unchecked")
		Map<String, Map<String, Integer>> raceExHpMap = (Map<String, Map<String, Integer>>) raceExMap.get(RaceEx.HP.name());
		if (raceExHpMap.containsKey(pokedexId)) {
			return raceExHpMap.get(pokedexId).get(RaceEx.fixed.name()).intValue();
		}

		// 小数点以下切り捨て
		double baseHp = baseHp(hp);

		if (correctFlg) {
			// 個体値が高い個体の補正後は四捨五入
			baseHp = tooStrongPokemonList.contains(pokedexId) ? Math.round(baseHp * 0.91) : baseHp;
		}

		return (int) baseHp;
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
	 * @param pokedexId
	 * @param correctFlg 強キャラ補正フラグ
	 * @return
	 */
	public int convGoAttack(int attack, int spAttack, int speed, String pokedexId, boolean correctFlg) {

		// 例外の固定値が存在する場合はその値を返却する。
		@SuppressWarnings("unchecked")
		Map<String, Map<String, Integer>> raceExAtMap = (Map<String, Map<String, Integer>>) raceExMap.get(RaceEx.ATTACK.name());
		if (raceExAtMap.containsKey(pokedexId)) {
			return raceExAtMap.get(pokedexId).get(RaceEx.fixed.name()).intValue();
		}

		double baseAttack = baseAttack(attack, spAttack, speed);

		if (correctFlg) {
			baseAttack = tooStrongPokemonList.contains(pokedexId) ? baseAttack * 0.91 : baseAttack;
		}

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
	 * @param pokedexId
	 * @param correctFlg 強キャラ補正フラグ
	 * @return
	 */
	public int convGoDefense(int defense, int spDefense, int speed, String pokedexId, boolean correctFlg) {

		// 例外の固定値が存在する場合はその値を返却する。
		@SuppressWarnings("unchecked")
		Map<String, Map<String, Integer>> raceExDfMap = (Map<String, Map<String, Integer>>) raceExMap.get(RaceEx.DEFENSE.name());
		if (raceExDfMap.containsKey(pokedexId)) {
			return raceExDfMap.get(pokedexId).get(RaceEx.fixed.name()).intValue();
		}

		double baseDefense = baseDefense(defense, spDefense, speed);

		if (correctFlg) {
			baseDefense = tooStrongPokemonList.contains(pokedexId) ? baseDefense * 0.91 : baseDefense;
		}

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
				convGoAttack(pokedex.getAttack(), pokedex.getSpecialAttack(), pokedex.getSpeed(), pokedex.getPokedexId(), true),
				convGoDefense(pokedex.getDefense(), pokedex.getSpecialDefense(), pokedex.getSpeed(), pokedex.getPokedexId(), true),
				convGoHp(pokedex.getHp(), pokedex.getPokedexId(), true),
				pl);
	}

	/**
	 * 原作の種族値からポケモンGOのPL50.5の場合のCPを求めます。<br>
	 * これは、ポケモンGOの基礎となる種族値です。<br>
	 * （このCPが4000を超えるかどうかが、種族値補正の基準になります。）<br>
	 * TODO: 個体値ALL0かつ、PL50.5	の時にCP4000以上だと実装に準拠する。が、謎だから正しいか確認したい。
	 *
	 * @param attack
	 * @param defense
	 * @param hp
	 * @param cpMultiplierMap
	 * @return
	 */
	public int culcBaseCpFromMain(Pokedex pokedex) {

		return pokemonGoUtils.culcCp(
				convGoAttack(pokedex.getAttack(), pokedex.getSpecialAttack(), pokedex.getSpeed(), pokedex.getPokedexId(), false),
				convGoDefense(pokedex.getDefense(), pokedex.getSpecialDefense(), pokedex.getSpeed(), pokedex.getPokedexId(), false),
				convGoHp(pokedex.getHp(), pokedex.getPokedexId(), false), "50.5");
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
				convGoAttack(pokedex.getAttack(), pokedex.getSpecialAttack(), pokedex.getSpeed(), pokedex.getPokedexId(), true),
				convGoDefense(pokedex.getDefense(), pokedex.getSpecialDefense(), pokedex.getSpeed(), pokedex.getPokedexId(), true),
				convGoHp(pokedex.getHp(), pokedex.getPokedexId(), true),
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
				convGoAttack(pokedex.getAttack(), pokedex.getSpecialAttack(), pokedex.getSpeed(), pokedex.getPokedexId(), true),
				convGoDefense(pokedex.getDefense(), pokedex.getSpecialDefense(), pokedex.getSpeed(), pokedex.getPokedexId(), true),
				convGoHp(pokedex.getHp(), pokedex.getPokedexId(), true));
	}
}
