package jp.brainjuice.pokego.business.service.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jp.brainjuice.pokego.business.dao.entity.GoPokedex;
import jp.brainjuice.pokego.business.dao.entity.Pokedex;
import jp.brainjuice.pokego.business.service.utils.memory.TooStrongPokemonList;
import jp.brainjuice.pokego.utils.BjUtils;
import jp.brainjuice.pokego.utils.exception.PokemonDataInitException;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class PokemonUtils {

	private TooStrongPokemonList tooStrongPokemonList;

	private PokemonGoUtils pokemonGoUtils;

	private Map<String, Object> raceExMap;

	private static final String FILE_NAME = "pokemon/race-exceptions.yml";

	// 強ポケ補正の基準になるPL
	private static final String TOO_STRONG_PL = "50.5";
	// 強ポケ補正の補正値
	private static final double TOO_STRONG_CORRECTION_VALUE = 0.91;
	// 強ポケ補正の補正値（メガ）
	private static final double TOO_STRONG_CORRECTION_VALUE_MEGA = 0.97;

	/**
	 * 起動時の依存関係の都合上存在しているコンストラクタ
	 *
	 * @param pokemonGoUtils
	 * @throws PokemonDataInitException
	 */
	public PokemonUtils (PokemonGoUtils pokemonGoUtils) throws PokemonDataInitException {
		this.pokemonGoUtils = pokemonGoUtils;

		init();
	}

	@Autowired
	public PokemonUtils(TooStrongPokemonList tooStrongPokemonList,
			PokemonGoUtils pokemonGoUtils) throws PokemonDataInitException {
		this.tooStrongPokemonList = tooStrongPokemonList;
		this.pokemonGoUtils = pokemonGoUtils;

		init();
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
		NOT_EXISTS_ORIGIN, // 原作に存在しないポケモンであるか否か
	}

	/**
	 * race-exceptions.ymlを読み取る。
	 *
	 * @throws PokemonDataInitException
	 */
	@SuppressWarnings("unchecked")
	public void init() throws PokemonDataInitException {

		raceExMap = new HashMap<String, Object>();
		try {
			raceExMap.putAll(BjUtils.loadYaml(FILE_NAME, Map.class));

			// 未設定の場合、空を設定する。
			Stream.of(RaceEx.values()).forEach(raceEx -> {
				raceExMap.putIfAbsent(raceEx.name(), new HashMap<>());
			});
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new PokemonDataInitException(e);
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
		goPokedex.setImage1(pokedex.getImage1());
		goPokedex.setImage2(pokedex.getImage2());
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
		Map<String, Integer> raceExHpMap = (Map<String, Integer>) raceExMap.get(pokedexId);
		if (raceExHpMap != null && raceExHpMap.containsKey(RaceEx.HP.name())) {
			return raceExHpMap.get(RaceEx.HP.name()).intValue();
		}

		double baseHp = baseHp(hp);

		if (correctFlg) {
			double correctionValue = PokemonEditUtils.isMega(pokedexId)
					? TOO_STRONG_CORRECTION_VALUE_MEGA : TOO_STRONG_CORRECTION_VALUE;
			// 個体値が高い個体の補正後は四捨五入
			baseHp = tooStrongPokemonList.contains(pokedexId) ? Math.round(baseHp * correctionValue) : baseHp;
		}

		// 小数点以下切り捨て
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
		Map<String, Integer> raceExAtMap = (Map<String, Integer>) raceExMap.get(pokedexId);
		if (raceExAtMap != null && raceExAtMap.containsKey(RaceEx.ATTACK.name())) {
			return raceExAtMap.get(RaceEx.ATTACK.name()).intValue();
		}

		double baseAttack = baseAttack(attack, spAttack, speed);

		if (correctFlg) {
			double correctionValue = PokemonEditUtils.isMega(pokedexId)
					? TOO_STRONG_CORRECTION_VALUE_MEGA : TOO_STRONG_CORRECTION_VALUE;
			baseAttack = tooStrongPokemonList.contains(pokedexId) ? baseAttack * correctionValue : baseAttack;
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
		Map<String, Integer> raceExDfMap = (Map<String, Integer>) raceExMap.get(pokedexId);
		if (raceExDfMap != null && raceExDfMap.containsKey(RaceEx.DEFENSE.name())) {
			return raceExDfMap.get(RaceEx.DEFENSE.name()).intValue();
		}

		double baseDefense = baseDefense(defense, spDefense, speed);

		if (correctFlg) {
			double correctionValue = PokemonEditUtils.isMega(pokedexId)
					? TOO_STRONG_CORRECTION_VALUE_MEGA : TOO_STRONG_CORRECTION_VALUE;
			baseDefense = tooStrongPokemonList.contains(pokedexId) ? baseDefense * correctionValue : baseDefense;
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
	public int calcCpFromMain(Pokedex pokedex, String pl) {
		return pokemonGoUtils.calcCp(
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
	public int calcBaseCpFromMain(Pokedex pokedex) {

		return pokemonGoUtils.calcCp(
				convGoAttack(pokedex.getAttack(), pokedex.getSpecialAttack(), pokedex.getSpeed(), pokedex.getPokedexId(), false),
				convGoDefense(pokedex.getDefense(), pokedex.getSpecialDefense(), pokedex.getSpeed(), pokedex.getPokedexId(), false),
				convGoHp(pokedex.getHp(), pokedex.getPokedexId(), false), TOO_STRONG_PL);
	}

	/**
	 * 原作の種族値からポケモンGOの最大CPを求めます。<br>
	 *
	 * @param pokedex
	 * @param pl
	 * @return
	 */
	public int calcMaxIvCpFromMain(Pokedex pokedex, String pl) {
		return pokemonGoUtils.calcMaxIvCp(
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
	public int calcMaxCpFromMain(Pokedex pokedex) {
		return pokemonGoUtils.calcMaxCp(
				convGoAttack(pokedex.getAttack(), pokedex.getSpecialAttack(), pokedex.getSpeed(), pokedex.getPokedexId(), true),
				convGoDefense(pokedex.getDefense(), pokedex.getSpecialDefense(), pokedex.getSpeed(), pokedex.getPokedexId(), true),
				convGoHp(pokedex.getHp(), pokedex.getPokedexId(), true));
	}

	/**
	 * 原作種族値が存在するかを判定する。
	 *
	 * @param pokedexId
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public boolean existsOrigin(String pokedexId) {

		if (!raceExMap.containsKey(pokedexId)) {
			return true;
		}

		Map<String, Boolean> notExistsOriginMap = (Map<String, Boolean>) raceExMap.get(pokedexId);

		// NOT_EXISTS_ORIGINのキーがある、かつtrueの場合のみ原作種族値が存在しない。
		return !(notExistsOriginMap.containsKey(RaceEx.NOT_EXISTS_ORIGIN.name())
				&& notExistsOriginMap.get(RaceEx.NOT_EXISTS_ORIGIN.name()));
	}
}
