package jp.brainjuice.pokego.business.service.utils.memory.evo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jp.brainjuice.pokego.business.dao.GoPokedexRepository;
import jp.brainjuice.pokego.business.dao.entity.GoPokedex;
import jp.brainjuice.pokego.business.service.utils.PokemonEditUtils;
import jp.brainjuice.pokego.business.service.utils.dto.evo.Hierarchy;
import jp.brainjuice.pokego.utils.exception.PokemonDataInitException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
class EvolutionInfo {

	private EvolutionList evoList;

	/** 進化前が複数する例外的なポケモンのマップ */
	private EvoExceptionsMap exceptionsMap;

	/** 進化前が存在しないポケモンのセット */
	private final Set<String> noEvoSet = new HashSet<>();

	/** 進化前のポケモンをvalueに持つマップ */
	private final Map<String, String> bfEvoMap = new HashMap<>();

	/** 最終進化のポケモンのセット（進化なしのポケモン含む） */
	private final Set<String> finalEvoSet = new HashSet<>();

	static final String ROOT = "root";

	@Autowired
	EvolutionInfo(
			EvolutionList evoList,
			EvoExceptionsMap evoExceptionsMap,
			GoPokedexRepository goPokedexRepository) throws PokemonDataInitException {
		init(evoList, evoExceptionsMap, goPokedexRepository);
		this.exceptionsMap = evoExceptionsMap;
		this.evoList = evoList;
	}

	/**
	 * 進化前のポケモンを取得する。
	 *
	 * @param pokedexId
	 * @return
	 */
	String getBeforeEvolution(String pokedexId) {

		if (noEvoSet.contains(pokedexId)) {
			// 進化前が存在しない場合
			return null;
		}

		return bfEvoMap.get(pokedexId);
	}

	/**
	 * 進化後のポケモンを取得する。
	 *
	 * @param pokedexId
	 * @return
	 */
	List<String> getAfterEvolution(String pokedexId) {

		return bfEvoMap.entrySet().stream()
				.filter(entry -> pokedexId.equals(entry.getValue()))
				.map(entry -> entry.getKey())
				.collect(Collectors.toList());

	}

	/**
	 * 進化後のポケモンが存在するか判定する。
	 *
	 * @param pokedexId
	 * @return
	 */
	boolean isAfterEvolution(String pokedexId) {

		// Stream#anyMatch(e -> true)でStream版のisEmpty()になる。
		return bfEvoMap.entrySet().stream()
				.filter(entry -> pokedexId.equals(entry.getValue()))
				.anyMatch(e -> true);
	}

	/**
	 * 別の「すがた」を取得する。
	 *
	 * @param pokedexId
	 * @return
	 */
	List<String> getAnotherFormList(String pokedexId) {

		// pokedexIdの前半4桁は図鑑№
		String pokedexNo = PokemonEditUtils.getStrPokedexNo(pokedexId);

		Stream<String> anotherFormStream = Stream.concat(
				bfEvoMap.entrySet().stream().map(Map.Entry::getKey), // 進化前が存在するポケモンのMap
				noEvoSet.stream()) // 進化しないポケモンのSet
				.filter(pid -> pokedexNo.equals(PokemonEditUtils.getStrPokedexNo(pid))); // 図鑑№が一致するポケモンを洗い出す。

		// 削除する対象のリスト
		List<String> removeList = Stream.concat(
				exceptionsMap.entrySet().stream().flatMap(entry -> entry.getValue().stream()), // 例外のダミーのpokedexIdを追加
				Stream.of(pokedexId)) // 自身も追加
				.collect(Collectors.toList());

		return anotherFormStream
				.filter(pid -> !removeList.contains(pid))
				.collect(Collectors.toList());
	}

	/**
	 * 進化ツリー上の最初のポケモンを取得する。
	 *
	 * @param pokedexId
	 * @return
	 */
	String getFirstInEvoTree(String pokedexId) {

		// 進化前を取得
		String bf = getBeforeEvolution(pokedexId);

		if (bf == null && pokedexId != null) {
			// 進化前が存在しない場合はpokedexIdを返却
			return pokedexId;
		}

		if (bf != null) {
			// 再帰的に呼び出し、進化のツリー上の最も最初のポケモンを取得する。
			bf = getFirstInEvoTree(bf);
		}

		return bf;
	}

	/**
	 * 進化ツリー上の最後のポケモンを取得する。
	 *
	 * @param pokedexId
	 * @return
	 */
	List<String> getLastInEvoTree(String pokedexId) {

		// ヒエラルキーの整理のために使用するクラス
		final TempHierarchyInfo thi = new TempHierarchyInfo();

		// 引数のpokedexIdを最終進化でないポケモンのリストに追加する。
		thi.getNotFinalEvoList().add(pokedexId);

		// 再帰的に呼び出し、最終進化のポケモンを取得する。
		List<String> retList = digHierarchyCallRecursively(thi);

		return retList;
	}

	/**
	 *
	 * 再帰的に次の階層に掘り進める。
	 * 進化のツリーにおいて、末端であることが確定しているポケモン、確定していないポケモンをリストに追加する。
	 *
	 * @param thi
	 */
	private List<String> digHierarchyCallRecursively(TempHierarchyInfo thi) {

		digHierarchyInfo(thi);

		if (!thi.getNotFinalEvoList().isEmpty()) {
			// 最終進化が確定していないポケモンがいる場合は再帰的に呼び出す。
			digHierarchyCallRecursively(thi);
		}

		return thi.getFinalEvoList();

	}

	/**
	 * １つ下の階層に掘り進める。
	 *
	 * @param thi
	 */
	private void digHierarchyInfo(TempHierarchyInfo thi) {

		List<String> finEvoList = new ArrayList<>();
		List<String> afNotFinEvoList = new ArrayList<>();

		// 最終進化でないポケモンをループさせる
		for (String pid: thi.getNotFinalEvoList()) {
			if (isAfterEvolution(pid)) {
				// 最終進化でない場合
				List<String> afterList = getAfterEvolution(pid);
				afNotFinEvoList.addAll(afterList);
			} else {
				// 最終進化の場合
				finEvoList.add(pid);
			}
		}

		// 最終進化でないポケモン、最終進化のポケモンをそれぞれ更新する。
		thi.setNotFinalEvoList(afNotFinEvoList);
		thi.setFinalEvoList(finEvoList);

	}

	/**
	 * ヒエラルキーの整理のために使用するクラス<br>
	 * 最終進化の一覧を作成するために使用する。
	 *
	 * @author saibabanagchampa
	 *
	 */
	@Data
	private class TempHierarchyInfo {
		/** 最終進化ポケモンリスト */
		private List<String> finalEvoList = new ArrayList<>();
		/** 最終進化でないポケモンのリスト */
		private List<String> notFinalEvoList = new ArrayList<>();
	}

	/**
	 * 最終進化のポケモンかを判定する。
	 *
	 * @param pokedexId
	 * @return
	 */
	boolean isFinalEvolution(String pokedexId) {
		return finalEvoSet.contains(pokedexId);
	}

	/**
	 * 最終進化のポケモンのセットを取得する。
	 *
	 * @return
	 */
	Set<String> getFinalEvoSet() {
		return new HashSet<String>(finalEvoSet);
	}

	/**
	 * 最終進化だけに絞り込む。
	 *
	 * @param goPokedexList
	 * @return
	 */
	List<GoPokedex> filterFinalEvoList(List<GoPokedex> goPokedexList) {

		return goPokedexList.stream()
				.filter(gp -> finalEvoSet.contains(gp.getPokedexId()))
				.collect(Collectors.toList());
	}


	/**
	 * 進化ツリー上の進化前、進化後のポケモンをすべて取得する。<br>
	 * 引数に設定したポケモンは排除する。
	 *
	 * @param pokedexId
	 * @return
	 */
	List<String> getBfAfEvoList(String pokedexId) {

		List<String> retList = new ArrayList<>();

		// 進化ツリー上の一番最初のポケモンをhieListに追加して初期化。
		String firstPokeId = getFirstInEvoTree(pokedexId);
		List<String> hieList = Arrays.asList(firstPokeId);

		while (!hieList.isEmpty()) {

			retList.addAll(hieList);

			// 進化後のポケモンをすべて取得
			hieList = hieList.stream()
					.flatMap(pid -> getAfterEvolution(pid).stream())
					.collect(Collectors.toList());
		}

		// 例外のダミーのpokedexIdを削除対象に追加。
		List<String> removeList = Stream.concat(
				exceptionsMap.entrySet().stream().flatMap(entry -> entry.getValue().stream()), // 例外のダミーのpokedexIdを追加
				Stream.of(pokedexId)) // 自身も追加
				.collect(Collectors.toList());
		retList.removeAll(removeList);

		return retList;
	}

	/**
	 * 同系統のすべてのポケモンを取得する。
	 *
	 * @param pokedexId
	 * @return
	 */
	List<String> getAllInEvoTree(String pokedexId) {

		// 別のすがたを取得 -> 進化前、進化後を取得 -> 別のすがたを取得 -> Setに変換 -> Listに変換。
		return Stream.concat(getAnotherFormList(pokedexId).stream(), Stream.of(pokedexId))
				.flatMap(pid -> Stream.concat(getBfAfEvoList(pid).stream(), Stream.of(pid)))
				.flatMap(pid -> Stream.concat(getAnotherFormList(pid).stream(), Stream.of(pid)))
				.collect(Collectors.toSet()).stream()
				.collect(Collectors.toList());

	}

	/**
	 * 同系統のポケモンにおける一意の図鑑№を取得する。<br>
	 * （その系統のポケモンの進化ツリー上の最も若い図鑑№を取得する。）
	 *
	 * @param pokedexId
	 * @return
	 */
	int basePokedexNo(String pokedexId) {
		List<String> allPokeIdList = getAllInEvoTree(pokedexId);
		allPokeIdList.sort(PokemonEditUtils.getPokedexIdComparator());
		return PokemonEditUtils.getPokedexNo(allPokeIdList.get(0));
	}

	/**
	 * Hierarchyのリストを取得する。<br>
	 * ガーメイルのような例外的に進化前が複数存在するポケモンに対する考慮済み。<br>
	 * ほとんどのポケモンは、一番外側のリストは1件のみになる。
	 *
	 * @param pokedexId
	 * @return
	 */
	List<List<List<Hierarchy>>> getEvoTrees(String pokedexId) {

		List<List<List<Hierarchy>>> treeList = null;

		// 例外ポケモンの場合はダミーのpokedexIdを取得
		List<String> excList = exceptionsMap.get(pokedexId);

		// 進化ツリーを取得する。
		if (excList == null) {
			// 例外（進化前が複数）のポケモンが存在しない場合
			treeList = Arrays.asList(getEvoTree(pokedexId));
		} else {
			// 例外のポケモン（ガーメイル等）
			treeList = Stream.concat(excList.stream(), Stream.of(pokedexId))
					.sorted(PokemonEditUtils.getPokedexIdComparator())
					.map(pid -> getEvoTree(pid))
					.collect(Collectors.toList());
		}

		/** ダミーのpokedexIdを正規のpokedexIdに変換する。*/
		// 直列化
		final List<Hierarchy> hieList = new ArrayList<>();
		for (List<List<Hierarchy>> yList: treeList) {
			for (List<Hierarchy> xList: yList) {
				hieList.addAll(xList);
			}
		}

		// 例外マップを見て正規のpokedexIdに変換する。
		for (Hierarchy hie: hieList) {
			for (Map.Entry<String, List<String>> excEntry: exceptionsMap.entrySet()) {
				// excEntryはKeyが正規のpokedexId, ValueがダミーのpokedexId
				if (excEntry.getValue().contains(hie.getId())) {
					hie.setId(excEntry.getKey());
				}
				if (excEntry.getValue().contains(hie.getBid())) {
					hie.setBid(excEntry.getKey());
				}

			}
		}

		return treeList;
	}

	/**
	 * Hierarchyのリストを取得する。<br>
	 * 外側のリストはy軸（第何進化）、内側のリストはx軸（分岐進化の別ポケモン）。
	 *
	 * @param pokedexId
	 * @return
	 */
	private List<List<Hierarchy>> getEvoTree(String pokedexId) {

		final List<List<Hierarchy>> yList = new ArrayList<>();

		// 進化ツリーを取得する。
		Map<Integer, Map<String, String>> hieMap = getEvoHierarchy(pokedexId);

		/** 進化ツリーをList<List<Hierarchy>>の形式に変換する */
		for (Map.Entry<Integer, Map<String, String>> hieEntry: hieMap.entrySet()) {

			List<Hierarchy> xList = hieEntry.getValue().entrySet().stream()
					.map(entry -> new Hierarchy(
							0, // x軸は一旦0で初期化
							hieEntry.getKey().intValue(),
							0, // x軸の距離も一旦0で初期化
							entry.getKey(),
							entry.getValue(),
							evoList.getCosts(entry.getKey(), entry.getValue()),
							evoList.canGoEvo(entry.getKey(), entry.getValue())))
					.collect(Collectors.toList());
			yList.add(xList);
		}


		/** 並び替え、x軸の位置、進化前のポケモンのx軸の距離を設定する。 */
		for (int y = 0, ySize = yList.size(); y < ySize; y++) {

			if (0 < y) {
				// 並び替え
				Collections.sort(yList.get(y), (o1, o2) -> {
					return PokemonEditUtils
							.getPokedexIdComparator()
							.compare(o1.getId(), o2.getId());
				});
			}

			// x軸、進化前のポケモンとのx軸方向の距離(dist)を算出し、セットする。
			for (int x = 0; x < yList.get(y).size(); x++) {
				final List<Hierarchy> xList = yList.get(y);

				final Hierarchy xItem = xList.get(x);
				if (y == 0) {
					// 第１階層
					xItem.setX(1);
				} else {
					// 第２階層以降

					// 進化前のポケモンのx軸を一旦引き継ぐ。
					int befX = 0;
					for (Hierarchy h: yList.get(y - 1)) {
						if (xItem.getBid().equals(h.getId())) {
							befX = h.getX();
							break;
						}
					}

					// 同じ進化前のポケモンが既にいる場合は、x軸を正の方向にずらす
					int max = befX;
					for (Hierarchy h: yList.get(y)) {
						if (h.getBid().equals(xItem.getBid()) && h != xItem && max <= h.getX()) {
							// 進化前が一致、かつ同一のポケモンでない場合は、最大値を更新する。
							max = h.getX() + 1;
						}
					}

					// x、distにセット
					xItem.setX(max);
					xItem.setDist(max - befX);
				}
			}
		}

		/** x軸の重複を解消する。（重複してる場合はいい感じにx軸の正の方向にずらす。） */
		// 処理の都合上、直列で持ち変える
		List<Hierarchy> parallelList = yList.stream()
				.flatMap(xList -> xList.stream())
				.collect(Collectors.toList());

		int i = 0;
		int size = parallelList.size();
		while (i < size - 1) {
			Hierarchy h = parallelList.get(i);

			boolean matchFlg = false;
			for (int i2 = i + 1; i2 < size; i2++) {
				Hierarchy h2 = parallelList.get(i2);

				int x = 0;
				if (h != h2 && h.getX() == h2.getX() && h.getY() == h2.getY()) {
					// x軸とy軸が一致する場合（同一インスタンスは除く）
					matchFlg = true;
					x = h2.getX();
				}

				if (matchFlg) {
					for (Hierarchy h3: parallelList) {
						// 重複している場合、それよりx軸で正の方向にある場合（進化前のポケモンが同一の場合を除く。）
						if (x <= h3.getX() && h.getBid() != h3.getBid()) {
							// x軸で正の方向に+1
							h3.setX(h3.getX() + 1);
							if (h3.getX() - h3.getDist() <= x) {
								// xをずらしたが、進化前はずらさない場合、distを+1する。
								h3.setDist(h3.getDist() + 1);
							}
						}
					}
					break;
				}
			}

			if (matchFlg) {
				// 重複解消後も、また重複が発生する可能性があるため、再度ループし直す。
				i = 0;
				continue;
			}

			i++;
		}

		return yList;
	}

	/**
	 * 進化ツリーを取得する。<br>
	 * { ${階層}: { ${pokedexId}: ${前の階層のpokedexId(第１階層の場合は"root")}}
	 *
	 * @param pokedexId
	 * @return
	 */
	Map<Integer, Map<String, String>> getEvoHierarchy(String pokedexId) {

		final Map<Integer, Map<String, String>> retMap = new HashMap<>();

		// 進化ツリーの一番最初のポケモン
		String firstPokeId = getFirstInEvoTree(pokedexId);

		int hierarchy = 1;

		// 第１階層
		{
			final Map<String, String> hieMap = new HashMap<>();
			hieMap.put(firstPokeId, ROOT);
			retMap.put(hierarchy, hieMap);
		}

		// 第２階層以降
		List<String> beforeHieList = new ArrayList<>();
		beforeHieList.add(firstPokeId);

		while (!beforeHieList.isEmpty()) {

			hierarchy++;

			// hieMapは、<その階層のpokedexId, 進化前のpokedexId>の情報を持つ。
			Map<String, String> hieMap = beforeHieList.stream()
					.flatMap(bfPid -> getAfterEvolution(bfPid).stream().map(afPid -> Map.entry(afPid, bfPid)))
					.collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue()));

			if (hieMap.isEmpty()) {
				break;
			}

			// <階数, <その階層のpokedexId, 次の階層のpokedexId>>
			retMap.put(Integer.valueOf(hierarchy), hieMap);

			// 次の階層の準備のため、進化後のpokedexIdを進化前のpokedexIdとする。
			beforeHieList = hieMap.entrySet().stream()
					.map(Map.Entry::getKey) // 進化後のpokedexId
					.collect(Collectors.toList());

		}

		return retMap;
	}

	/**
	 * 簡易形式の進化のツリーを取得する。
	 * 例：{0280N01={0281N01={0282N01=null, 0475N01=null}}}
	 *
	 * @param pokedexId
	 * @return
	 */
	Map<String, Object> getEvoTreeModel(String pokedexId) {

		final Map<String, Object> retMap = new HashMap<String, Object>();

		Map<Integer, Map<String, String>> hierarchyMap = getEvoHierarchy(pokedexId);

		// 階層数
		int hieCount = hierarchyMap.keySet().stream().max(Comparator.naturalOrder()).get().intValue();
		// 階層ごとにループ
		for (int y = 1; y <= hieCount; y++) {
			// 階層内のポケモンの情報でループ
			hierarchyMap.get(y).entrySet().stream().forEach(entry -> {

				Map<String, Object> scopedMap = searchMap(entry.getValue(), retMap);
				if (scopedMap == null) {
					// nullの場合はputを試みる。
					putKey(entry.getValue(), entry.getKey(), retMap);
				} else {
					// 該当するkeyを持つマップが見つかった場合、そのmapにputを試みる。
					putKey(entry.getValue(), entry.getKey(), scopedMap);
				}
			});
		}

		return retMap;
	}

	/**
	 * Mapにキーをputする。
	 *
	 * @param key
	 * @param valueKey
	 * @param map
	 */
	@SuppressWarnings("unchecked")
	private void putKey(String key, String valueKey, Map<String, Object> map) {

		Map<String, Object> tmpMap;

		// 第一階層の考慮
		if (ROOT.equals(key)) {
			map.put(valueKey, null);
			return;
		}

		if (map.containsKey(key) && (Map<String, Object>) map.get(key) != null) {
			// { key: { valueKey: null }}のうち、keyとvalueKeyが存在する場合
			tmpMap = (Map<String, Object>) map.get(key);
			tmpMap.put(valueKey, null);
		} else {
			// { key: { valueKey: null }}のうち、keyが存在しない場合、またはkeyは存在するがvalueKeyが存在しない場合
			tmpMap = new HashMap<>();
			tmpMap.put(valueKey, null);
			map.put(key, tmpMap);
		}
	}

	/**
	 * マップの階層の中から該当するキーを含んでいるマップを取得する。
	 *
	 * @param pokedexId
	 * @param tree 例：{0280N01={0281N01={0282N01=null, 0475N01=null}}}
	 * @return
	 */
	private Map<String, Object> searchMap(String pokedexId, Map<String, Object> tree) {

		if (tree.containsKey(pokedexId)) {
			// 今の階層のマップに第1引数に指定したpokedexIdが含まれていた場合。
			return tree;
		}

		// 次の階層のマップでループ
		for (String pokeId: tree.keySet()) {
			@SuppressWarnings("unchecked")
			Map<String, Object> next = (Map<String, Object>) tree.get(pokeId);
			if (next == null) {
				continue;
			} else if (next.containsKey(pokedexId)) {
				return next;
			}

			Map<String, Object> result = searchMap(pokedexId, next);
			if (result != null && result.containsKey(pokedexId)) {
				return result;
			}
		}

		// ツリーの末端に到達したが、見つからなかった場合。
		return null;
	}

	/**
	 * 画面表示用のリストに変換する。引数にはEvolutionInfo#getEvoHierarchy(String)で取得した三次元リストを指定する。
	 * x軸、y軸の二次元のリストが複数ある状態を引数に受け取り、その二次元リストの空欄部分にnullを追加する。
	 *
	 * @param hieList
	 * @return
	 */
	List<List<List<Hierarchy>>> convDispHierarchy(List<List<List<Hierarchy>>> hieList) {

		List<List<List<Hierarchy>>> retList = new ArrayList<>();

		// リスト全体のループ
		for (List<List<Hierarchy>> yList: hieList) {

			// x座標の最大要素数（表の横幅）を求める。
			int max = yList.stream()
					.map(xList -> xList.size())
					.max(Comparator.naturalOrder())
					.get();

			List<List<Hierarchy>> retYList = new ArrayList<>();
			// y座標のループ
			for (List<Hierarchy> xList: yList) {
				// x座標の値を一旦すべて返却用のリストに追加する。
				List<Hierarchy> retXList = new ArrayList<>(xList);
				// 空欄部分にnullを追加する。
				retXList.addAll(Collections.nCopies(max - xList.size(), null));

				retYList.add(retXList);
			}

			retList.add(retYList);
		}

		return retList;
	}

	/**
	 * 起動時に実行。
	 *
	 * @throws PokemonDataInitException
	 */
	private void init(
			EvolutionList evoList,
			EvoExceptionsMap exceptionsMap,
			GoPokedexRepository goPokedexRepository) throws PokemonDataInitException {

		Set<String> noEvoSet = new HashSet<String>();
		Map<String, String> bfEvoMap = new HashMap<String, String>();
		evoList.forEach(evo -> {
			if (evo.getBeforePokedexId().isEmpty()) {
				// 進化前が存在しない場合
				noEvoSet.add(evo.getPokedexId());
			} else {
				// 進化前が存在する場合
				bfEvoMap.put(evo.getPokedexId(), evo.getBeforePokedexId());
			}
		});
		this.noEvoSet.addAll(noEvoSet);
		this.bfEvoMap.putAll(bfEvoMap);


		this.finalEvoSet.addAll(createNoBfEvoMap(noEvoSet, bfEvoMap, exceptionsMap));

		log.info("EvolutionInfo generated!! (Referenced file: none.)");
	}

	/**
	 * noBfEvoMapを生成する。
	 *
	 * @param noEvoSet
	 * @param bfEvoMap
	 * @return noBfEvoMap
	 */
	private Set<String> createNoBfEvoMap(
			Set<String> noEvoSet,
			Map<String, String> bfEvoMap,
			Map<String, List<String>> exceptionsMap) {

		final Set<String> noBfEvoMap = new HashSet<>();

		// 進化前が存在しないポケモンのセットから進化後が存在しないポケモンを抜き出す。
		noBfEvoMap.addAll(noEvoSet.stream().filter(
				pid1 -> !isAfterEvolution(pid1)).collect(Collectors.toList()));

		// 進化前のポケモンをvalueに持つマップから進化後が存在しないポケモンを抜き出す。
		noBfEvoMap.addAll(bfEvoMap.entrySet().stream().filter(
				entry1 -> !isAfterEvolution(entry1.getKey())).map(
						entry2 -> entry2.getKey()).collect(Collectors.toList()));

		// ダミーのpokedexIdを削除する。
		List<String> dummyIdList = new ArrayList<>();
		exceptionsMap.forEach((k, v) -> v.forEach(dummyId -> dummyIdList.add(dummyId)));
		noBfEvoMap.removeAll(dummyIdList);

		return noBfEvoMap;
	}

}
