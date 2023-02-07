package jp.brainjuice.pokego.business.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import jp.brainjuice.pokego.business.dao.entity.Pokedex;
import jp.brainjuice.pokego.business.dao.entity.PokedexFilterInfo;
import jp.brainjuice.pokego.business.service.utils.PokemonEditUtils;
import jp.brainjuice.pokego.business.service.utils.PokemonEditUtils.RegionEnum;
import jp.brainjuice.pokego.business.service.utils.memory.EvolutionInfo;
import jp.brainjuice.pokego.business.service.utils.memory.TypeMap;
import jp.brainjuice.pokego.business.service.utils.memory.TypeMap.TypeEnum;
import lombok.extern.slf4j.Slf4j;

/**
 * pokedexIdの絞り込みをするためのリポジトリクラス。
 *
 * @author saibabanagchampa
 *
 */
@Repository
@Slf4j
public class PokedexFilterInfoRepository implements CrudRepository<PokedexFilterInfo, String> {

	/** 絞り込み用Pokdexリスト */
	private final List<PokedexFilterInfo> fPokedexes = new ArrayList<>();

	/**
	 * 絞り込みに使用できるキーワード
	 *
	 * @author saibabanagchampa
	 *
	 */
	public enum FilterEnum {
		/** 最終進化<br>Boolean */
		finalEvo,
		/** メガシンカ<br>Boolean */
		mega,
		/** 実装済み<br>Boolean */
		impled,
		/** 地域<br>String */
		region,
		/** タイプ<br>String */
		type,
		/** 2タイプ<br>List&lt;String&gt;(2elements) */
		twoType,
	}

	/**
	 * 絞り込み用のPredicateを持つマップ（カリー化）
	 */
	// Boolean型のキーワード
	private final Map<FilterEnum, Function<Boolean, Predicate<PokedexFilterInfo>>> boolFieldMap = new HashMap<>();
	// String型のキーワード
	private final Map<FilterEnum, Function<String, Predicate<PokedexFilterInfo>>> stringFieldMap = new HashMap<>();
	// String型のキーワードを２つ指定
	private final Map<FilterEnum, Function<String, Function<String, Predicate<PokedexFilterInfo>>>> twoStrFieldMap = new HashMap<>();
	{
		boolFieldMap.put(FilterEnum.finalEvo, (b) -> (pfi) -> pfi.isFinalEvo() == b.booleanValue());
		boolFieldMap.put(FilterEnum.mega, (b) -> (pfi) -> pfi.isMega() == b.booleanValue());
		boolFieldMap.put(FilterEnum.impled, (b) -> (pfi) -> pfi.isImpled() == b.booleanValue());

		stringFieldMap.put(FilterEnum.region, (s) -> (pfi) -> pfi.getRegion().name().equals(s));
		stringFieldMap.put(FilterEnum.type, (s) -> (pfi) -> s.equals(pfi.getType1().name()) || s.equals(pfi.getType2().name()));

		twoStrFieldMap.put(FilterEnum.twoType, (type1) -> (type2) -> (pfi) -> (type1.equals(pfi.getType1().name()) && type2.equals(pfi.getType2().name()))
				|| (type1.equals(pfi.getType2().name()) && type2.equals(pfi.getType1().name())));
	}

	/**
	 * DI用コンストラクタ
	 *
	 * @param pokedexRepository
	 * @param typeMap
	 * @param evolutionInfo
	 * @param pokemonEditUtils
	 */
	@Autowired
	public PokedexFilterInfoRepository(
			PokedexRepository pokedexRepository,
			TypeMap typeMap,
			EvolutionInfo evolutionInfo,
			PokemonEditUtils pokemonEditUtils) {

		init(pokedexRepository, typeMap, evolutionInfo, pokemonEditUtils);

		log.info("PokedexFilterInfo table generated!!");
	}

	/**
	 * タイプからpokedexIdを絞り込む。
	 *
	 * @param type
	 * @return
	 */
	public List<String> findIdByType(TypeEnum type) {
		return fPokedexes.stream()
				.filter(pfi -> type.equals(pfi.getType1()) || type.equals(pfi.getType2()))
				.map(pfi2 -> pfi2.getPokedexId())
				.collect(Collectors.toList());
	}

	/**
	 * ２つのタイプからpokedexIdを絞り込む。
	 *
	 * @param type1
	 * @param type2
	 * @return
	 */
	public List<String> findIdByType(TypeEnum type1, TypeEnum type2) {
		return fPokedexes.stream()
				.filter(pfi -> (type1.equals(pfi.getType1()) && type2.equals(pfi.getType2()))
						|| (type1.equals(pfi.getType2()) && type2.equals(pfi.getType1())))
				.map(pfi2 -> pfi2.getPokedexId())
				.collect(Collectors.toList());
	}

	/**
	 * 引数に設定したMapを使用し、pokedexIdを絞り込む。
	 *
	 * @param values Key: PokedexFieldInfoのフィールド名, Value: FilterEnum参照
	 * @return
	 */
	public List<String> findByAny(Map<FilterEnum, Object> values) {

		Stream<PokedexFilterInfo> stream = fPokedexes.stream();
		// フィルターで絞り込んでいく
		for(Map.Entry<FilterEnum, Object> entry: values.entrySet()) {
			FilterEnum key = entry.getKey();
			if (boolFieldMap.containsKey(key)) {
				// Boolean型
				stream = stream.filter(boolFieldMap.get(key).apply((Boolean) entry.getValue()));
			} else if (stringFieldMap.containsKey(key)) {
				// String型
				stream = stream.filter(stringFieldMap.get(key).apply((String) entry.getValue()));
			} else if (twoStrFieldMap.containsKey(key)) {
				// String型2つ
				@SuppressWarnings("unchecked")
				List<String> vList = (List<String>) entry.getValue();
				stream = stream.filter(twoStrFieldMap.get(key).apply(vList.get(0)).apply(vList.get(1)));
			}
		}
		return stream.map(pfi -> pfi.getPokedexId()).collect(Collectors.toList());
	}

	/**
	 * @deprecated 未実装
	 */
	@Override
	public <S extends PokedexFilterInfo> S save(S entity) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	public <S extends PokedexFilterInfo> Iterable<S> saveAll(Iterable<S> entities) {
		entities.forEach(fPokedexes::add);
		return entities;
	}

	@Override
	public Optional<PokedexFilterInfo> findById(String id) {
		return fPokedexes.stream().filter(p -> p.getPokedexId().equals(id)).findAny();
	}

	/**
	 * @deprecated 未実装
	 */
	@Override
	public boolean existsById(String id) {
		// TODO 自動生成されたメソッド・スタブ
		return false;
	}

	@Override
	public Iterable<PokedexFilterInfo> findAll() {
		return new ArrayList<>(fPokedexes);
	}

	@Override
	public Iterable<PokedexFilterInfo> findAllById(Iterable<String> ids) {
		return fPokedexes.stream().filter(p -> {
			boolean exists = false;
			for (String pid: ids) {
				exists = p.getPokedexId().equals(pid);
				if (exists) break;
			}
			return exists;
		}).collect(Collectors.toList());
	}

	@Override
	public long count() {
		return fPokedexes.size();
	}

	/**
	 * @deprecated 未実装
	 */
	@Override
	public void deleteById(String id) {
		// TODO 自動生成されたメソッド・スタブ

	}

	/**
	 * @deprecated 未実装
	 */
	@Override
	public void delete(PokedexFilterInfo entity) {
		// TODO 自動生成されたメソッド・スタブ

	}

	/**
	 * @deprecated 未実装
	 */
	@Override
	public void deleteAllById(Iterable<? extends String> ids) {
		// TODO 自動生成されたメソッド・スタブ

	}

	/**
	 * @deprecated 未実装
	 */
	@Override
	public void deleteAll(Iterable<? extends PokedexFilterInfo> entities) {
		// TODO 自動生成されたメソッド・スタブ

	}

	/**
	 * @deprecated 未実装
	 */
	@Override
	public void deleteAll() {
		// TODO 自動生成されたメソッド・スタブ

	}

	/**
	 * すべてのポケモン情報を登録する。
	 *
	 * @param pokedexRepository
	 * @param typeMap
	 * @param evolutionInfo
	 * @param pokemonEditUtils
	 */
	public void init(PokedexRepository pokedexRepository, TypeMap typeMap, EvolutionInfo evolutionInfo, PokemonEditUtils pokemonEditUtils) {

		List<Pokedex> pokeList = pokedexRepository.findAll();

		List<PokedexFilterInfo> pfiList = pokeList.stream().map(p -> {
			String pid = p.getPokedexId();
			String subspecies = pokemonEditUtils.getSubspecies(pid);
			PokedexFilterInfo pfi = new PokedexFilterInfo();
			pfi.setPokedexId(pid);
			pfi.setFinalEvo(evolutionInfo.isAfterEvolution(pid));
			pfi.setMega(pokemonEditUtils.isMega(pid));
			pfi.setImpled(p.isImplFlg());
			pfi.setRegion(RegionEnum.getEnumName(subspecies));
			pfi.setType1(TypeEnum.getEnumName(p.getType1()));
			pfi.setType2(TypeEnum.getEnumName(p.getType2()));

			return pfi;
		}).collect(Collectors.toList());

		saveAll(pfiList);
	}

}
