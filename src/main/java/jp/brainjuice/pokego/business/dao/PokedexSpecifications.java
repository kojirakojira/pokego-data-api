package jp.brainjuice.pokego.business.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jp.brainjuice.pokego.business.constant.GenNameEnum;
import jp.brainjuice.pokego.business.constant.RegionEnum;
import jp.brainjuice.pokego.business.constant.Type.TypeEnum;
import jp.brainjuice.pokego.business.dao.dto.FilterParam;
import jp.brainjuice.pokego.business.dao.entity.Pokedex;
import jp.brainjuice.pokego.business.dao.entity.PokedexFilterInfo;
import jp.brainjuice.pokego.business.service.utils.PokemonEditUtils;
import jp.brainjuice.pokego.business.service.utils.dto.type.TwoTypeKey;
import jp.brainjuice.pokego.business.service.utils.memory.TooStrongPokemonList;
import jp.brainjuice.pokego.business.service.utils.memory.evo.EvolutionProvider;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * 独自の仕様なのでSpringのSpecificationとは全く別物です。
 * PokedexまたはGoPokedexを絞り込むために使用するクラスです。
 *
 * @author saibabanagchampa
 *
 */
@Component
@Slf4j
public class PokedexSpecifications {

	/** 絞り込み用Pokdexリスト */
	private final List<PokedexFilterInfo> fPokedexes = new ArrayList<>();

	/**
	 * 絞り込みに使用できるキーワード
	 *
	 * @author saibabanagchampa
	 *
	 */
	@AllArgsConstructor
	public enum FilterEnum {
		/**
		 * タイプ<br>String or TypeEnum or List<String><br>List検索はor演算
		 * @see TypeEnum
		 */
		type("タイプ"),
		/**
		 * 2タイプ<br>List&lt;String&gt;(2elements)
		 * @see TypeEnum
		 */
		twoType("タイプ"),
		/**
		 * 最終進化<br>Boolean<br>※trueの場合のみ絞り込む。falseの場合は絞り込みは実施しない。
		 */
		finEvo("最終進化"),
		/**
		 * メガシンカ<br>Boolean<br>※trueの場合のみ絞り込む。falseの場合は絞り込みは実施しない。
		 */
		mega("メガシンカ"),
		/**
		 * 実装済み<br>Boolean<br>※trueの場合のみ絞り込む。falseの場合は絞り込みは実施しない。
		 */
		impled("PokémonGO実装済み"),
		/**
		 * 強ポケ補正<br>Boolean<br>※trueの場合のみ絞り込む。falseの場合は絞り込みは実施しない。
		 */
		tooStrong("強ポケ補正"),
		/**
		 * 地域<br>String or RegionEnum or List<String><br>List検索はor演算
		 * @see RegionEnum
		 */
		region("地域"),
		/**
		 * 世代<br>String or GenNameEnum or List<String><br>List検索はor演算
		 * @see GenNameEnum
		 */
		gen("世代"),
		;

		@Getter
		private String jpn;
	}

	/**
	 * 絞り込み用のPredicateを持つマップ（カリー化）
	 */
	// Boolean型のキーワード
	private final Map<FilterEnum, Predicate<PokedexFilterInfo>> boolKeywordMap = new HashMap<>();
	// String型のキーワード
	private final Map<FilterEnum, Function<String, Predicate<PokedexFilterInfo>>> stringKeywordMap = new HashMap<>();
	// String型のキーワードを２つ指定
	private final Map<FilterEnum, Function<String, Function<String, Predicate<PokedexFilterInfo>>>> twoStrKeywordMap = new HashMap<>();
	{
		boolKeywordMap.put(FilterEnum.finEvo, (pfi) -> pfi.isFinalEvo()); // 最終進化
		boolKeywordMap.put(FilterEnum.mega, (pfi) -> pfi.isMega()); // メガシンカ
		boolKeywordMap.put(FilterEnum.impled, (pfi) -> pfi.isImpled()); // 実装済み
		boolKeywordMap.put(FilterEnum.tooStrong, (pfi) -> pfi.isTooStrong()); // 実装済み

		stringKeywordMap.put(FilterEnum.region, (s) -> (pfi) -> s.equals(pfi.getRegion().name())); // 地域
		stringKeywordMap.put(FilterEnum.type, (s) -> (pfi) -> s.equals(pfi.getType1Name()) || s.equals(pfi.getType2Name())); // タイプ
		stringKeywordMap.put(FilterEnum.gen, (s) -> (pfi) -> s.equals(pfi.getGen().name())); // 世代

		twoStrKeywordMap.put(FilterEnum.twoType, (type1) -> (type2) -> (pfi) -> (type1.equals(pfi.getType1Name()) && type2.equals(pfi.getType2Name()))
				|| (type1.equals(pfi.getType2Name()) && type2.equals(pfi.getType1Name()))); // 2タイプ

	}

	/**
	 * DI用コンストラクタ
	 *
	 * @param pokedexRepository
	 * @param typeMap
	 * @param evolutionProvider
	 * @param pokemonEditUtils
	 */
	@Autowired
	public PokedexSpecifications(
			PokedexRepository pokedexRepository,
			EvolutionProvider evolutionProvider,
			TooStrongPokemonList tooStrongPokemonList) {

		init(pokedexRepository, evolutionProvider, tooStrongPokemonList);

		log.info("PokedexSpecifications generated!!");
	}

	/**
	 * タイプからpokedexIdを絞り込む。
	 *
	 * @param type
	 * @return
	 */
	List<String> findIdByType(TypeEnum type) {
		return fPokedexes.stream()
				.filter(pfi -> type.equals(pfi.getType1()) || type.equals(pfi.getType2()))
				.map(pfi2 -> pfi2.getPokedexId())
				.collect(Collectors.toList());
	}

	/**
	 * ２つのタイプからpokedexIdを絞り込む。
	 *
	 * @param twoTypeKey
	 * @return
	 */
	List<String> findIdByType(TwoTypeKey twoTypeKey) {

		TypeEnum type1 = twoTypeKey.getType1();
		TypeEnum type2 = twoTypeKey.getType2();

		return fPokedexes.stream()
				.filter(pfi -> (type1 == pfi.getType1() && type2 == pfi.getType2())
						|| (type1 == pfi.getType2() && type2 == pfi.getType1()))
				.map(pfi2 -> pfi2.getPokedexId())
				.collect(Collectors.toList());
	}

	/**
	 * 引数に設定したMapを使用し、pokedexIdを絞り込む。
	 *
	 * @param values 検索値
	 * @return
	 * @see FilterEnum
	 * @see FilterParam
	 */
	@SuppressWarnings("unchecked")
	List<String> findIdByAny(Map<FilterEnum, FilterParam> values) {

		Stream<PokedexFilterInfo> stream = fPokedexes.stream();

		// OR演算での絞り込み
		{
			Iterator<Entry<FilterEnum, FilterParam>> ite = values.entrySet().iterator();
			while (ite.hasNext()) {

				Map.Entry<FilterEnum, FilterParam> entry = ite.next();
				final FilterEnum key = entry.getKey();
				final Object value = entry.getValue().getFilterValue();
				final boolean negate = entry.getValue().isNegate();

				// String型のキーワード、かつ検索値がListにより複数指定された場合のみOR演算で絞り込む。
				if (!(stringKeywordMap.containsKey(key) && value instanceof List)) {
					continue;
				}

				Predicate<PokedexFilterInfo> predicate = null;
				for (Object v : (List<Object>) value) {
					predicate = or(predicate, stringKeywordMap.get(key).apply(PokemonEditUtils.getStrName(v)));
				}

				// キーワードごとに絞り込む（ORはこのタイミングで否定しなければならない。）
				stream = stream.filter(negate ? predicate.negate() : predicate);
				// 絞り込んだキーワードを削除する（そのためにIteratorでループさせてる）
				ite.remove();
			}
		}

		// AND演算での絞り込み
		{
			Predicate<PokedexFilterInfo> predicate = null;

			for(Map.Entry<FilterEnum, FilterParam> entry: values.entrySet()) {

				FilterEnum key = entry.getKey();
				Object value = entry.getValue().getFilterValue();
				boolean negate = entry.getValue().isNegate();

				if (boolKeywordMap.containsKey(key) && (boolean) value) {
					// Boolean型
					predicate = and(predicate, boolKeywordMap.get(key), negate);
				} else if (stringKeywordMap.containsKey(key)) {
					// String型（List<String>も可）
					predicate = and(predicate, stringKeywordMap.get(key).apply(PokemonEditUtils.getStrName(value)), negate);
				} else if (twoStrKeywordMap.containsKey(key)) {
					// String型2つ
					List<String> vList = ((List<?>) value).stream().map(PokemonEditUtils::getStrName).collect(Collectors.toList());
					predicate = and(predicate, twoStrKeywordMap.get(key).apply(vList.get(0)).apply(vList.get(1)), negate);
				}
			}
			if (predicate != null) {
				stream = stream.filter(predicate);
			}
		}

		return stream
				.map(pfi -> pfi.getPokedexId())
				.collect(Collectors.toList());
	}

	private Predicate<PokedexFilterInfo> and(Predicate<PokedexFilterInfo> origin, Predicate<PokedexFilterInfo> integrated, boolean negate) {
		integrated = negate ? integrated.negate() : integrated;
		return origin == null ? integrated : origin.and(integrated);
	}

	private Predicate<PokedexFilterInfo> or(Predicate<PokedexFilterInfo> origin, Predicate<PokedexFilterInfo> integrated) {
		return origin == null ? integrated : origin.or(integrated);
	}

	/**
	 * すべてのポケモン情報を登録する。
	 *
	 * @param pokedexRepository
	 * @param evolutionProvider
	 * @param pokemonEditUtils
	 */
	public void init(
			PokedexRepository pokedexRepository,
			EvolutionProvider evolutionProvider,
			TooStrongPokemonList tooStrongPokemonList) {

		List<Pokedex> pokeList = pokedexRepository.findAll();

		List<PokedexFilterInfo> pfiList = pokeList.stream().map(p -> {
			String pid = p.getPokedexId();
			String subspecies = PokemonEditUtils.getSubspecies(pid);
			PokedexFilterInfo pfi = new PokedexFilterInfo();
			pfi.setPokedexId(pid);
			pfi.setType1(TypeEnum.getType(p.getType1()));
			pfi.setType2(TypeEnum.getType(p.getType2()));
			pfi.setFinalEvo(!evolutionProvider.isAfterEvolution(pid));
			pfi.setMega(PokemonEditUtils.isMega(pid));
			pfi.setImpled(p.isImplFlg());
			pfi.setTooStrong(tooStrongPokemonList.contains(pid));
			pfi.setRegion(RegionEnum.getEnumName(subspecies));
			pfi.setGen(GenNameEnum.valueOf(p.getGen()));

			return pfi;
		}).collect(Collectors.toList());

		pfiList.forEach(fPokedexes::add);
	}

}
