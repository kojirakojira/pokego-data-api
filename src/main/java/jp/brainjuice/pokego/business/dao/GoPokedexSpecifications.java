
package jp.brainjuice.pokego.business.dao;

import java.util.List;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jp.brainjuice.pokego.business.constant.GenNameEnum;
import jp.brainjuice.pokego.business.constant.RegionEnum;
import jp.brainjuice.pokego.business.constant.Type.TypeEnum;
import jp.brainjuice.pokego.business.dao.entity.GoPokedex;
import jp.brainjuice.pokego.business.service.utils.PokemonEditUtils;

/**
 * GoPokedexを絞り込むために使用するクラス
 *
 * @author saibabanagchampa
 *
 */
@Component
public class GoPokedexSpecifications {

	private static final String POKEDEX_ID = "pokedex_id";

	private static final String REGION = "region";

	private static final String TYPE1 = "type1";

	private static final String TYPE2 = "type2";

	private static final String GEN = "gen";

	private static final String IMPL_FLG = "impl_flg";

	private static final String TOO_STRONG = "too_strong";

	private static final String FIN_EVO = "fin_evo";

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
	 * (イメージ：WHERE region = 'N')
	 *
	 * @param bool
	 * @return
	 */
	public Specification<GoPokedex> regionEqual(RegionEnum region) {
		return (root, query, builder) -> builder.equal(root.get(REGION), region.getCode());
	}

	/**
	 * 地域
	 * (イメージ：WHERE region <> 'N')
	 *
	 * @param bool
	 * @return
	 */
	public Specification<GoPokedex> regionNotEqual(RegionEnum region) {
		return (root, query, builder) -> builder.not(builder.equal(root.get(REGION), region.getCode()));
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
					regionList.stream().map(RegionEnum::getCode).toList());
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
					regionList.stream().map(RegionEnum::getCode).toList()));
		};
	}

	/**
	 * 世代
	 * (イメージ：WHERE gen = 'g1')
	 *
	 * @param bool
	 * @return
	 */
	public Specification<GoPokedex> genEqual(GenNameEnum gen) {
		return (root, query, builder) -> builder.equal(root.get(GEN), gen.name());
	}

	/**
	 * 世代
	 * (イメージ：WHERE gen <> 'g1')
	 *
	 * @param bool
	 * @return
	 */
	public Specification<GoPokedex> genNotEqual(GenNameEnum gen) {
		return (root, query, builder) -> builder.not(builder.equal(root.get(GEN), gen.name()));
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
					regionList.stream().map(GenNameEnum::name).toList());
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
					regionList.stream().map(GenNameEnum::name).toList()));
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
	private CriteriaBuilder.In<String> stringInPredicate(Root<GoPokedex> root, CriteriaBuilder builder, List<String> list) {

		CriteriaBuilder.In<String> inClause = builder.in(root.get(REGION));
		for (String str: list) {
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
	 * (イメージ：WHERE (type1 = :type1 AND type2 = :type2) OR (type1 = :type2 AND type2 = :type1))
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

//
//	/**
//	 * 絞り込み用のPredicateを持つマップ（カリー化）
//	 */
//	// Boolean型のキーワード
//	private final Map<FilterEnum, Predicate<PokedexFilterInfo>> boolKeywordMap = new HashMap<>();
//	// String型のキーワード
//	private final Map<FilterEnum, Function<String, Predicate<PokedexFilterInfo>>> stringKeywordMap = new HashMap<>();
//	// String型のキーワードを２つ指定
//	private final Map<FilterEnum, Function<String, Function<String, Predicate<PokedexFilterInfo>>>> twoStrKeywordMap = new HashMap<>();
//	{
//		boolKeywordMap.put(FilterEnum.finEvo, (pfi) -> pfi.isFinalEvo()); // 最終進化
//		boolKeywordMap.put(FilterEnum.mega, (pfi) -> pfi.isMega()); // メガシンカ
//		boolKeywordMap.put(FilterEnum.impled, (pfi) -> pfi.isImpled()); // 実装済み
//		boolKeywordMap.put(FilterEnum.tooStrong, (pfi) -> pfi.isTooStrong()); // 実装済み
//
//		stringKeywordMap.put(FilterEnum.region, (s) -> (pfi) -> s.equals(pfi.getRegion().name())); // 地域
//		stringKeywordMap.put(FilterEnum.type, (s) -> (pfi) -> s.equals(pfi.getType1Name()) || s.equals(pfi.getType2Name())); // タイプ
//		stringKeywordMap.put(FilterEnum.gen, (s) -> (pfi) -> s.equals(pfi.getGen().name())); // 世代
//
//		twoStrKeywordMap.put(FilterEnum.twoType, (type1) -> (type2) -> (pfi) -> (type1.equals(pfi.getType1Name()) && type2.equals(pfi.getType2Name()))
//				|| (type1.equals(pfi.getType2Name()) && type2.equals(pfi.getType1Name()))); // 2タイプ
//
//	}
//
//	/**
//	 * DI用コンストラクタ
//	 *
//	 * @param pokedexRepository
//	 * @param typeMap
//	 * @param evolutionProvider
//	 * @param pokemonEditUtils
//	 */
//	@Autowired
//	public PokedexSpecifications(
//			PokedexRepository pokedexRepository,
//			EvolutionProvider evolutionProvider,
//			TooStrongPokemonList tooStrongPokemonList) {
//
//		init(pokedexRepository, evolutionProvider, tooStrongPokemonList);
//
//		log.info("PokedexSpecifications generated!! (Referenced file: none.)");
//	}
//
//	/**
//	 * タイプからpokedexIdを絞り込む。
//	 *
//	 * @param type
//	 * @return
//	 */
//	List<String> findIdByType(TypeEnum type) {
//		return fPokedexes.stream()
//				.filter(pfi -> type.equals(pfi.getType1()) || type.equals(pfi.getType2()))
//				.map(pfi2 -> pfi2.getPokedexId())
//				.collect(Collectors.toList());
//	}
//
//	/**
//	 * ２つのタイプからpokedexIdを絞り込む。
//	 *
//	 * @param twoTypeKey
//	 * @return
//	 */
//	List<String> findIdByType(TwoTypeKey twoTypeKey) {
//
//		TypeEnum type1 = twoTypeKey.getType1();
//		TypeEnum type2 = twoTypeKey.getType2();
//
//		return fPokedexes.stream()
//				.filter(pfi -> (type1 == pfi.getType1() && type2 == pfi.getType2())
//						|| (type1 == pfi.getType2() && type2 == pfi.getType1()))
//				.map(pfi2 -> pfi2.getPokedexId())
//				.collect(Collectors.toList());
//	}
//
//	/**
//	 * 引数に設定したMapを使用し、pokedexIdを絞り込む。
//	 *
//	 * @param values 検索値
//	 * @return
//	 * @see FilterEnum
//	 * @see FilterParam
//	 */
//	@SuppressWarnings("unchecked")
//	List<String> findIdByAny(Map<FilterEnum, FilterParam> values) {
//
//		Stream<PokedexFilterInfo> stream = fPokedexes.stream();
//
//		// OR演算での絞り込み
//		{
//			Iterator<Entry<FilterEnum, FilterParam>> ite = values.entrySet().iterator();
//			while (ite.hasNext()) {
//
//				Map.Entry<FilterEnum, FilterParam> entry = ite.next();
//				final FilterEnum key = entry.getKey();
//				final Object value = entry.getValue().getFilterValue();
//				final boolean negate = entry.getValue().isNegate();
//
//				// String型のキーワード、かつ検索値がListにより複数指定された場合のみOR演算で絞り込む。
//				if (!(stringKeywordMap.containsKey(key) && value instanceof List)) {
//					continue;
//				}
//
//				Predicate<PokedexFilterInfo> predicate = null;
//				for (Object v : (List<Object>) value) {
//					predicate = or(predicate, stringKeywordMap.get(key).apply(PokemonEditUtils.getStrName(v)));
//				}
//
//				// キーワードごとに絞り込む（ORはこのタイミングで否定しなければならない。）
//				stream = stream.filter(negate ? predicate.negate() : predicate);
//				// 絞り込んだキーワードを削除する（そのためにIteratorでループさせてる）
//				ite.remove();
//			}
//		}
//
//		// AND演算での絞り込み
//		{
//			Predicate<PokedexFilterInfo> predicate = null;
//
//			for(Map.Entry<FilterEnum, FilterParam> entry: values.entrySet()) {
//
//				FilterEnum key = entry.getKey();
//				Object value = entry.getValue().getFilterValue();
//				boolean negate = entry.getValue().isNegate();
//
//				if (boolKeywordMap.containsKey(key) && (boolean) value) {
//					// Boolean型
//					predicate = and(predicate, boolKeywordMap.get(key), negate);
//				} else if (stringKeywordMap.containsKey(key)) {
//					// String型（List<String>も可）
//					predicate = and(predicate, stringKeywordMap.get(key).apply(PokemonEditUtils.getStrName(value)), negate);
//				} else if (twoStrKeywordMap.containsKey(key)) {
//					// String型2つ
//					List<String> vList = ((List<?>) value).stream().map(PokemonEditUtils::getStrName).collect(Collectors.toList());
//					predicate = and(predicate, twoStrKeywordMap.get(key).apply(vList.get(0)).apply(vList.get(1)), negate);
//				}
//			}
//			if (predicate != null) {
//				stream = stream.filter(predicate);
//			}
//		}
//
//		return stream
//				.map(pfi -> pfi.getPokedexId())
//				.collect(Collectors.toList());
//	}
//
//	private Predicate<PokedexFilterInfo> and(Predicate<PokedexFilterInfo> origin, Predicate<PokedexFilterInfo> integrated, boolean negate) {
//		integrated = negate ? integrated.negate() : integrated;
//		return origin == null ? integrated : origin.and(integrated);
//	}
//
//	private Predicate<PokedexFilterInfo> or(Predicate<PokedexFilterInfo> origin, Predicate<PokedexFilterInfo> integrated) {
//		return origin == null ? integrated : origin.or(integrated);
//	}
//
//	/**
//	 * すべてのポケモン情報を登録する。
//	 *
//	 * @param pokedexRepository
//	 * @param evolutionProvider
//	 * @param pokemonEditUtils
//	 */
//	public void init(
//			PokedexRepository pokedexRepository,
//			EvolutionProvider evolutionProvider,
//			TooStrongPokemonList tooStrongPokemonList) {
//
//		List<Pokedex> pokeList = pokedexRepository.findAll();
//
//		List<PokedexFilterInfo> pfiList = pokeList.stream().map(p -> {
//			String pid = p.getPokedexId();
//			String subspecies = PokemonEditUtils.getSubspecies(pid);
//			PokedexFilterInfo pfi = new PokedexFilterInfo();
//			pfi.setPokedexId(pid);
//			pfi.setType1(p.getType1());
//			pfi.setType2(p.getType2());
//			pfi.setFinalEvo(!evolutionProvider.isAfterEvolution(pid));
//			pfi.setMega(PokemonEditUtils.isMega(pid));
//			pfi.setImpled(p.isImplFlg());
//			pfi.setTooStrong(tooStrongPokemonList.contains(pid));
//			pfi.setRegion(RegionEnum.getEnumName(subspecies));
//			pfi.setGen(p.getGen());
//
//			return pfi;
//		}).collect(Collectors.toList());
//
//		pfiList.forEach(fPokedexes::add);
//	}

}
