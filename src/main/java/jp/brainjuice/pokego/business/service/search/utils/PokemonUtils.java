package jp.brainjuice.pokego.business.service.search.utils;

import java.util.Map;

import org.springframework.stereotype.Component;

import jp.brainjuice.pokego.cache.inmemory.RaceExceptionsMap;
import jp.brainjuice.pokego.cache.inmemory.dto.RaceEx;
import jp.brainjuice.pokego.dao.jpa.TooStrongRepository;
import jp.brainjuice.pokego.dao.jpa.entity.GoPokedex;
import jp.brainjuice.pokego.dao.jpa.entity.Pokedex;
import jp.brainjuice.pokego.utils.BjUtils;

@Component
public class PokemonUtils {

	private TooStrongRepository tooStrongRepository;

	private PokemonGoUtils pokemonGoUtils;

	private RaceExceptionsMap raceExceptionsMap;

	// 強ポケ補正の基準になるPL
	private static final String TOO_STRONG_PL = "50.5";
	// 強ポケ補正の補正値
	private static final double TOO_STRONG_CORRECTION_VALUE = 0.91;
	// 強ポケ補正の補正値（メガ）
	private static final double TOO_STRONG_CORRECTION_VALUE_MEGA = 0.97;

	public PokemonUtils(TooStrongRepository tooStrongRepository,
			PokemonGoUtils pokemonGoUtils,
			RaceExceptionsMap raceExceptionsMap) {
		this.tooStrongRepository = tooStrongRepository;
		this.pokemonGoUtils = pokemonGoUtils;
		this.raceExceptionsMap = raceExceptionsMap;
	}

	/**
	 * GOステータスを取得します。
	 *
	 * @param pokedex
	 * @return
	 */
	public GoPokedex getGoPokedex(Pokedex pokedex) {

		int attack = convGoAttack(
				pokedex,
				true);

		int defense = convGoDefense(
				pokedex,
				true);

		int hp = convGoHp(pokedex, true);

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
		goPokedex.setImage1(BjUtils.replaceEmpty(pokedex.getImage1()));
		goPokedex.setImage2(BjUtils.replaceEmpty(pokedex.getImage2()));
		goPokedex.setImplFlg(pokedex.isImplFlg());

		return goPokedex;
	}

	/**
	 * 原作→Go HP変換
	 *
	 * @param pokedex
	 * @param correctFlg 強キャラ補正フラグ
	 * @return
	 */
	public int convGoHp(Pokedex pokedex, boolean correctFlg) {

		String pid = pokedex.getPokedexId();
		// 例外の固定値が存在する場合はその値を返却する。
		Map<RaceEx, Object> raceExHpMap = raceExceptionsMap.get(pid);
		if (raceExHpMap != null && raceExHpMap.containsKey(RaceEx.HP)) {
			return ((Integer) raceExHpMap.get(RaceEx.HP)).intValue();
		}

		double baseHp = baseHp(pokedex.getHp());

		if (correctFlg) {
			double correctionValue = PokemonEditUtils.isMega(pokedex)
					? TOO_STRONG_CORRECTION_VALUE_MEGA : TOO_STRONG_CORRECTION_VALUE;
			// 強ポケ補正後は四捨五入
			baseHp = tooStrongRepository.existsById(pid) ? Math.round(baseHp * correctionValue) : baseHp;
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
	 * @param pokedex
	 * @param correctFlg 強キャラ補正フラグ
	 * @return
	 */
	public int convGoAttack(Pokedex pokedex, boolean correctFlg) {

		String pid = pokedex.getPokedexId();
		// 例外の固定値が存在する場合はその値を返却する。
		Map<RaceEx, Object> raceExAtMap = raceExceptionsMap.get(pid);
		if (raceExAtMap != null && raceExAtMap.containsKey(RaceEx.ATTACK)) {
			return ((Integer) raceExAtMap.get(RaceEx.ATTACK)).intValue();
		}

		double baseAttack = baseAttack(pokedex.getAttack(), pokedex.getSpecialAttack(), pokedex.getSpeed());
		System.out.println(baseAttack);
		if (correctFlg) {
			double correctionValue = PokemonEditUtils.isMega(pokedex)
					? TOO_STRONG_CORRECTION_VALUE_MEGA : TOO_STRONG_CORRECTION_VALUE;
			baseAttack = tooStrongRepository.existsById(pid) ? baseAttack * correctionValue : baseAttack;
		}

		System.out.println(Math.round(baseAttack));
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
	 * @param pokedex
	 * @param correctFlg 強キャラ補正フラグ
	 * @return
	 */
	public int convGoDefense(Pokedex pokedex, boolean correctFlg) {

		String pid = pokedex.getPokedexId();
		// 例外の固定値が存在する場合はその値を返却する。
		Map<RaceEx, Object> raceExDfMap = raceExceptionsMap.get(pid);
		if (raceExDfMap != null && raceExDfMap.containsKey(RaceEx.DEFENSE)) {
			return ((Integer) raceExDfMap.get(RaceEx.DEFENSE)).intValue();
		}

		double baseDefense = baseDefense(pokedex.getDefense(), pokedex.getSpecialDefense(), pokedex.getSpeed());

		if (correctFlg) {
			double correctionValue = PokemonEditUtils.isMega(pokedex)
					? TOO_STRONG_CORRECTION_VALUE_MEGA : TOO_STRONG_CORRECTION_VALUE;
			baseDefense = tooStrongRepository.existsById(pid) ? baseDefense * correctionValue : baseDefense;
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
				convGoAttack(pokedex, true),
				convGoDefense(pokedex, true),
				convGoHp(pokedex, true),
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
				convGoAttack(pokedex, false),
				convGoDefense(pokedex, false),
				convGoHp(pokedex, false), TOO_STRONG_PL);
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
				convGoAttack(pokedex, true),
				convGoDefense(pokedex, true),
				convGoHp(pokedex, true),
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
				convGoAttack(pokedex, true),
				convGoDefense(pokedex, true),
				convGoHp(pokedex, true));
	}

	/**
	 * 原作種族値が存在するかを判定する。
	 *
	 * @param pokedexId
	 * @return
	 */
	public boolean existsOrigin(String pokedexId) {

		if (!raceExceptionsMap.containsKey(pokedexId)) {
			return true;
		}

		Map<RaceEx, Object> notExistsOriginMap = raceExceptionsMap.get(pokedexId);

		// NOT_EXISTS_ORIGINのキーがある、かつtrueの場合のみ原作種族値が存在しない。
		return !(notExistsOriginMap.containsKey(RaceEx.NOT_EXISTS_ORIGIN)
				&& ((Boolean) notExistsOriginMap.get(RaceEx.NOT_EXISTS_ORIGIN)).booleanValue());
	}
}
