package jp.brainjuice.pokego.business.service.utils.memory;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Component;

import jp.brainjuice.pokego.business.dao.GoPokedexRepository;
import jp.brainjuice.pokego.business.dao.entity.GoPokedex;
import jp.brainjuice.pokego.business.service.utils.PokemonEditUtils;
import jp.brainjuice.pokego.business.service.utils.dto.Evolution;
import jp.brainjuice.pokego.business.service.utils.dto.Hierarchy;
import jp.brainjuice.pokego.utils.BjCsvMapper;
import jp.brainjuice.pokego.utils.BjUtils;
import jp.brainjuice.pokego.utils.exception.PokemonDataInitException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class EvolutionInfo {

	/** 進化前が存在しないポケモンのセット */
	private final Set<String> noEvoSet = new HashSet<>();

	/** 進化前のポケモンをvalueに持つマップ */
	private final Map<String, String> bfEvoMap = new HashMap<>();

	/** 最終進化のポケモンのセット（進化なしのポケモン含む） */
	private final Set<String> finalEvoSet = new HashSet<>();

	/** 進化前が複数する例外的なポケモンのマップ */
	private final Map<String, List<String>> exceptionsMap = new HashMap<>();

	private final List<Evolution> evoList = new ArrayList<>();

	private static final String FILE_NAME = "pokemon/pokemon-evolution.csv";

	private static final String EXCEPTIONS_FILE_NAME = "pokemon/pokemon-evolution-exceptions.yml";

	private static final String ROOT = "root";

	private static final String NOT_EXISTS_MSG = "pokemon.csvに定義したポケモンがpokemon-evolution.csvに定義されていません。{0}";

	private static final String COSTS_CANDY_MSG = "アメ{0}個";

	public EvolutionInfo(GoPokedexRepository goPokedexRepository) throws PokemonDataInitException {
		init(goPokedexRepository);
	}

	/**
	 * 進化前のポケモンを取得する。
	 *
	 * @param pokedexId
	 * @return
	 */
	public String getBeforeEvolution(String pokedexId) {

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
	public List<String> getAfterEvolution(String pokedexId) {

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
	public boolean isAfterEvolution(String pokedexId) {

		// Stream#anyMatch(e -> true)でStream版のisEmpty()になる。
		return bfEvoMap.entrySet().stream()
				.filter(entry -> pokedexId.equals(entry.getValue())).anyMatch(e -> true);

	}

	/**
	 * 別の「すがた」を取得する。
	 *
	 * @param pokedexId
	 * @return
	 */
	public List<String> getAnotherFormList(String pokedexId) {

		final List<String> anotherFormList = new ArrayList<>();

		// pokedexIdの前半4桁は図鑑№
		String pokedexNo = PokemonEditUtils.getStrPokedexNo(pokedexId);

		// 進化前が存在するポケモンのMapから、図鑑№が一致するポケモンを洗い出す。
		bfEvoMap.forEach((k, v) -> {
			if (pokedexNo.equals(PokemonEditUtils.getStrPokedexNo(k))) {
				anotherFormList.add(k);
			}
		});

		// 進化しないポケモンのSetから、図鑑№が一致するポケモンを洗い出す。
		noEvoSet.stream().filter(pokeId -> pokedexNo.equals(PokemonEditUtils.getStrPokedexNo(pokeId))).forEach(anotherFormList::add);

		/** 同じpokedexId、ダミーのpokedexIdは排除する。 */
		// 削除する対象のリスト
		final List<String> removeList = new ArrayList<>();
		// 例外のダミーのpokedexIdを追加。
		exceptionsMap.forEach((k, v) -> {
			removeList.addAll(v);
		});
		// 検索対象のpokedexIdも追加。
		removeList.add(pokedexId);
		// すべて削除
		anotherFormList.removeAll(removeList);

		return anotherFormList;
	}

	/**
	 * 進化ツリー上の最初のポケモンを取得する。
	 *
	 * @param pokedexId
	 * @return
	 */
	public String getFirstInEvoTree(String pokedexId) {

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
	public List<String> getLastInEvoTree(String pokedexId) {

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
	 * @return
	 */
	private Map<String, List<String>> digHierarchyInfo(TempHierarchyInfo thi) {

		final Map<String, List<String>> retMap = new HashMap<>();

		// 最終進化でないポケモンのリスト
		final List<String> notFinalEvoList = new ArrayList<>();

		thi.getNotFinalEvoList().forEach(pokeId -> {
			if (isAfterEvolution(pokeId)) {
				// 最終進化でない
				List<String> afterList = getAfterEvolution(pokeId);
				notFinalEvoList.addAll(afterList);
				retMap.put(pokeId, afterList);
			} else {
				// 最終進化
				thi.getFinalEvoList().add(pokeId);
			}
		});

		// 最終進化でないポケモンを更新する。
		thi.setNotFinalEvoList(notFinalEvoList);

		return retMap;

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
		private final List<String> finalEvoList = new ArrayList<>();
		/** 最終進化でないポケモンのリスト */
		private List<String> notFinalEvoList = new ArrayList<>();
	}

	/**
	 * 最終進化のポケモンかを判定する。
	 *
	 * @param pokedexId
	 * @return
	 */
	public boolean isFinalEvolution(String pokedexId) {
		return finalEvoSet.contains(pokedexId);
	}

	/**
	 * 最終進化のポケモンのセットを取得する。
	 *
	 * @return
	 */
	public Set<String> getFinalEvoSet() {
		return new HashSet<String>(finalEvoSet);
	}

	/**
	 * 最終進化だけに絞り込む。
	 *
	 * @param goPokedexList
	 * @return
	 */
	public List<GoPokedex> filterFinalEvoList(List<GoPokedex> goPokedexList) {

		return goPokedexList.stream().filter(
				poke -> finalEvoSet.contains(poke.getPokedexId())).collect(Collectors.toList());
	}


	/**
	 * 進化ツリー上の進化前、進化後のポケモンをすべて取得する。<br>
	 * 引数に設定したポケモンは排除する。
	 *
	 * @param pokedexId
	 * @return
	 */
	public List<String> getBfAfEvoList(String pokedexId) {

		final List<String> retList = new ArrayList<>();

		// 進化ツリーの一番最初のポケモン
		final String firstPokeId = getFirstInEvoTree(pokedexId);

		List<String> hieList = new ArrayList<>();
		hieList.add(firstPokeId);

		while (!hieList.isEmpty()) {
			final List<String> tmpList = new ArrayList<>();

			hieList.forEach(pokeId -> {
				tmpList.addAll(getAfterEvolution(pokeId));
			});

			retList.addAll(hieList);

			hieList = tmpList;
		}
		final List<String> removeList = new ArrayList<>();
		// 例外のダミーのpokedexIdを削除対象に追加。
		exceptionsMap.forEach((k, v) -> {
			removeList.addAll(v);
		});
		removeList.add(pokedexId);
		retList.removeAll(removeList);

		return retList;
	}

	/**
	 * 同系統のすべてのポケモンを取得する。
	 *
	 * @param pokedexId
	 * @return
	 */
	public List<String> getAllInEvoTree(String pokedexId) {

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
	public int basePokedexNo(String pokedexId) {
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
	public List<List<List<Hierarchy>>> getEvoTrees(String pokedexId) {

		final List<List<List<Hierarchy>>> treeList = new ArrayList<>();

		// 例外ポケモンの場合はダミーのpokedexIdを取得
		List<String> excList = exceptionsMap.get(pokedexId);

		if (excList == null) {
			// 例外でない場合
			treeList.add(getEvoTree(pokedexId));
		} else {
			// 例外の場合（ガーメイル）
			// 検索対象のpokedexIdも追加して並び替え
			excList = new ArrayList<>(excList);
			excList.add(pokedexId);
			Collections.sort(excList, PokemonEditUtils.getPokedexIdComparator());
			// ツリー取得
			excList.forEach(pokeId -> {
				treeList.add(getEvoTree(pokeId));
			});
		}

		/** ダミーのpokedexIdを正規のpokedexIdに変換する。*/
		// 直列化
		final List<Hierarchy> hieList = new ArrayList<>();
		treeList.forEach(yList -> {
			yList.forEach(xList -> {
				hieList.addAll(xList);
			});
		});
		// 例外マップを見て正規のpokedexIdに変換する。
		exceptionsMap.forEach((k, v) -> {
			hieList.forEach(hie -> {
				if (v.contains(hie.getId())) {
					hie.setId(k);
				}
				if (v.contains(hie.getBid())) {
					hie.setBid(k);
				}
			});
		});

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
		hieMap.entrySet().stream().forEach(hieEntry -> {
			List<Hierarchy> xList = hieEntry.getValue().entrySet().stream()
					.map(entry -> new Hierarchy(
							0, // x軸は一旦0で初期化
							hieEntry.getKey().intValue(),
							0, // x軸の距離も一旦0で初期化
							entry.getKey(),
							entry.getValue(),
							getCosts(entry.getKey(), entry.getValue())))
					.collect(Collectors.toList());
			yList.add(xList);
		});

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
	public Map<Integer, Map<String, String>> getEvoHierarchy(String pokedexId) {

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
	public Map<String, Object> getEvoTreeModel(String pokedexId) {

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
	 * その進化ツリー上のすべての注釈（evoAnnotation）を取得する。
	 * @param pids
	 * @return
	 */
	public List<String> getEvoAnnotations(Collection<String> pids) {

		List<String> retList = new ArrayList<>();
		for (String pid: pids) {
			for (Evolution evo: evoList) {
				if (pid.equals(evo.getPokedexId()) && !evo.getEvoAnnotations().isEmpty()) {
					retList.add(evo.getEvoAnnotations());
					break;
				}
			}
		}
		return retList;
	}

	/**
	 * 進化条件をリスト形式で取得する。
	 *
	 * @param id 図鑑ID
	 * @param bid 進化前ポケモンの図鑑ID
	 * @return
	 */
	private List<String> getCosts(String id, String bid) {

		List<String> retList = new ArrayList<>();

		if (ROOT.equals(bid)) {
			// rootの場合は進化前は存在しない。
			return retList;
		}

		// 対象のEvolutionを取得
		Evolution evo = evoList.stream()
				.filter(ev -> Objects.equals(ev.getPokedexId(), id) && Objects.equals(ev.getBeforePokedexId(), bid))
				.findFirst().get();

		// 進化アイテム
		BjUtils.addList(evo.getEvolutionItems(), retList);

		// 相棒としてのアクション
		BjUtils.addList(evo.getBuddy(), retList);

		// ルアーモジュール
		BjUtils.addList(evo.getLureModules(), retList);

		// 交換
		BjUtils.addList(evo.getTradeEvolution(), retList);

		// 特殊な条件
		BjUtils.addList(evo.getSpecialAction(), retList);

		// アメ
		BjUtils.addList(
				String.valueOf(evo.getCandy()),
				retList,
				(str) -> MessageFormat.format(COSTS_CANDY_MSG, str));

		return retList;
	}

	/**
	 * 起動時に実行。
	 *
	 * @throws PokemonDataInitException
	 */
	private void init(GoPokedexRepository goPokedexRepository) throws PokemonDataInitException {

		// CSVファイルの内容をメモリに抱える。
		try {
			List<Evolution> evolutionList = BjCsvMapper.mapping(FILE_NAME, Evolution.class);

			// pokemon.csvに定義したポケモンが、すべてpokemon-evolution.csvに定義されていることを確認する。
			checkAllExists(evolutionList, goPokedexRepository);

			evoList.addAll(evolutionList);

			Set<String> noEvoSet = new HashSet<String>();
			Map<String, String> bfEvoMap = new HashMap<String, String>();
			evolutionList.forEach(evo -> {
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

		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new PokemonDataInitException(e);
		}

		// yamlファイルの内容をメモリに抱える。
		try {
			@SuppressWarnings("unchecked")
			Map<String, List<String>> yamlMap = BjUtils.loadYaml(EXCEPTIONS_FILE_NAME, Map.class);
			exceptionsMap.putAll(yamlMap);

		} catch (Exception e) {
			throw new PokemonDataInitException(e);
		}


		this.finalEvoSet.addAll(createNoBfEvoMap(noEvoSet, bfEvoMap, exceptionsMap));

		log.info("EvolutionInfo generated!!");
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

	/**
	 * pokemon.csvに定義したポケモンが、すべてpokemon-evolution.csvに定義されていることを確認する。
	 *
	 * @param evoList
	 * @param goPokedexRepository
	 * @return
	 * @throws PokemonDataInitException
	 */
	private void checkAllExists(List<Evolution> evoList, GoPokedexRepository goPokedexRepository) throws PokemonDataInitException {

		List<String> evoPidList = evoList.stream()
				.map(Evolution::getPokedexId)
				.collect(Collectors.toList());

		List<GoPokedex> notExistsGpList = goPokedexRepository.findAll().stream()
				.filter(gp -> !evoPidList.contains(gp.getPokedexId()))
				.collect(Collectors.toList());

		// GoPokedexリストに存在していて、Evolutionリストに存在していないポケモンがいるかどうか。
		if (!notExistsGpList.isEmpty()) {
			throw new PokemonDataInitException(
					MessageFormat.format(
							NOT_EXISTS_MSG,
							notExistsGpList.stream().map(PokemonEditUtils::appendRemarks).collect(Collectors.toList())));
		}
	}

}
