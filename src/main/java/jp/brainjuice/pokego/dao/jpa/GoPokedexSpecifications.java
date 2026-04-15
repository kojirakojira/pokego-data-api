
package jp.brainjuice.pokego.dao.jpa;

import java.util.Date;
import java.util.List;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import jp.brainjuice.pokego.business.constant.GenNameEnum;
import jp.brainjuice.pokego.business.constant.RegionEnum;
import jp.brainjuice.pokego.business.constant.Type.TypeEnum;
import jp.brainjuice.pokego.business.service.search.utils.PokemonEditUtils;
import jp.brainjuice.pokego.dao.jpa.entity.GoPokedex;

/**
 * GoPokedexを絞り込むために使用するクラス
 *
 * @author saibabanagchampa
 *
 */
@Component
public class GoPokedexSpecifications {

	private static final String POKEDEX_ID = "pokedexId";

	private static final String REGION = "region";

	private static final String TYPE1 = "type1";

	private static final String TYPE2 = "type2";

	private static final String GEN = "gen";

	private static final String DYNAMAX = "dynamaxImplFlg";

	private static final String GIGANTAMX = "gigantamaxImplFlg";

	private static final String IMPL_FLG = "implFlg";

	private static final String RELEASE_DATE = "releaseDate";

	private static final String TOO_STRONG = "tooStrong";

	private static final String FIN_EVO = "finEvo";

	public Specification<GoPokedex> selectId() {
		return (root, query, builder) -> {
			Predicate predicate = builder.conjunction();

			query.select(root.get(POKEDEX_ID));

			return predicate;
		};
	}

	/**
	 * 最終進化
	 * (イメージ：WHERE fin_evo = true）
	 *
	 * @param bool
	 * @return
	 */
	public Specification<GoPokedex> finEvoEqual(boolean bool) {
		return (root, query, builder) -> builder.equal(root.get(FIN_EVO), bool);
	}

	/**
	 * メガシンカ
	 * (イメージ：WHERE region = 'M'）
	 *
	 * @param bool
	 * @return
	 */
	public Specification<GoPokedex> megaEqual(boolean bool) {
		return (root, query, builder) -> {
			Expression<?> expression = root.get(REGION);
			String m = PokemonEditUtils.M;
			return bool ? builder.equal(expression, m) : builder.notEqual(expression, m);
		};
	}

	/**
	 * ダイマックス
	 * (イメージ：WHERE dynamax_impl_flg = true）
	 *
	 * @param bool
	 * @return
	 */
	public Specification<GoPokedex> dynamaxEqual(boolean bool) {
		return (root, query, builder) -> builder.equal(root.get(DYNAMAX), bool);
	}

	/**
	 * リリース年月（開始）
	 * 
	 * @param date
	 * @return
	 */
	public Specification<GoPokedex> greaterThanEqual(Date date) {
		return (root, query, builder) -> builder.greaterThanOrEqualTo(root.get(RELEASE_DATE), date);
	}

	/**
	 * リリース年月（終了）
	 * 
	 * @param date
	 * @return
	 */
	public Specification<GoPokedex> lessThanEqual(Date date) {
		return (root, query, builder) -> builder.lessThanOrEqualTo(root.get(RELEASE_DATE), date);
	}

	/**
	 * キョダイマックス
	 * (イメージ：WHERE gigantamax_impl_flg = true）
	 *
	 * @param bool
	 * @return
	 */
	public Specification<GoPokedex> gigantamaxEqual(boolean bool) {
		return (root, query, builder) -> builder.equal(root.get(GIGANTAMX), bool);
	}

	/**
	 * 実装済みか否か
	 * (イメージ：WHERE impl_flg = true)
	 *
	 * @param bool
	 * @return
	 */
	public Specification<GoPokedex> implFlgEqual(boolean bool) {
		return (root, query, builder) -> builder.equal(root.get(IMPL_FLG), bool);
	}

	/**
	 * 強ポケ補正対象か否か
	 * (イメージ：WHERE too_strong = true)
	 *
	 * @param bool
	 * @return
	 */
	public Specification<GoPokedex> tooStrongEqual(boolean bool) {
		return (root, query, builder) -> builder.equal(root.get(TOO_STRONG), bool);
	}

	/**
	 * 地域
	 * (イメージ：WHERE region IN ('N', 'A'))
	 *
	 * @param bool
	 * @return
	 */
	public Specification<GoPokedex> regionIn(List<RegionEnum> regionList) {
		return (root, query, builder) -> {
			return stringInPredicate(
					root,
					builder,
					regionList.stream().map(RegionEnum::getCode).toList(),
					REGION);
		};
	}

	/**
	 * 地域
	 * (イメージ：WHERE region NOT IN ('N', 'A'))
	 *
	 * @param bool
	 * @return
	 */
	public Specification<GoPokedex> regionNotIn(List<RegionEnum> regionList) {
		return (root, query, builder) -> {
			return builder.not(stringInPredicate(
					root,
					builder,
					regionList.stream().map(RegionEnum::getCode).toList(),
					REGION));
		};
	}

	/**
	 * 世代
	 * (イメージ：WHERE gen IN ('g1', 'g2'))
	 *
	 * @param bool
	 * @return
	 */
	public Specification<GoPokedex> genIn(List<GenNameEnum> regionList) {
		return (root, query, builder) -> {
			return stringInPredicate(
					root,
					builder,
					regionList.stream().map(GenNameEnum::name).toList(),
					GEN);
		};
	}

	/**
	 * 世代
	 * (イメージ：WHERE gen IN ('g1', 'g2'))
	 *
	 * @param bool
	 * @return
	 */
	public Specification<GoPokedex> genNotIn(List<GenNameEnum> regionList) {
		return (root, query, builder) -> {
			return builder.not(stringInPredicate(
					root,
					builder,
					regionList.stream().map(GenNameEnum::name).toList(),
					GEN));
		};
	}

	/**
	 * 文字列のカラムへのIN句を生成する。
	 *
	 * @param root
	 * @param builder
	 * @param regionList
	 * @return
	 */
	private CriteriaBuilder.In<String> stringInPredicate(Root<GoPokedex> root, CriteriaBuilder builder,
			List<String> list, String column) {

		CriteriaBuilder.In<String> inClause = builder.in(root.get(column));
		for (String str : list) {
			inClause.value(str);
		}
		return inClause;
	}

	/**
	 * タイプ
	 * (イメージ：WHERE type1 = :type OR type2 = :type)
	 *
	 * @param bool
	 * @return
	 */
	public Specification<GoPokedex> typeEqual(TypeEnum type) {
		return (root, query, builder) -> builder.or(
				builder.equal(root.get(TYPE1), type),
				builder.equal(root.get(TYPE2), type));
	}

	/**
	 * 2タイプ
	 * (イメージ：WHERE (type1 = :type1 AND type2 = :type2) OR (type1 = :type2 AND type2
	 * = :type1))
	 *
	 * @param bool
	 * @return
	 */
	public Specification<GoPokedex> twoTypeEqual(TypeEnum type1, TypeEnum type2) {
		return (root, query, builder) -> {

			Predicate predicate1 = builder.and(
					builder.equal(root.get(TYPE1), type1),
					builder.equal(root.get(TYPE2), type2));

			Predicate predicate2 = builder.and(
					builder.equal(root.get(TYPE1), type2),
					builder.equal(root.get(TYPE2), type1));

			return builder.or(predicate1, predicate2);
		};
	}

}
