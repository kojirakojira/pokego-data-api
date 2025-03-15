package jp.brainjuice.pokego.business.service.utils.evo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import jp.brainjuice.pokego.business.dao.EvolutionRepository;
import jp.brainjuice.pokego.business.dao.GoPokedexRepository;
import jp.brainjuice.pokego.business.dao.entity.Evolution;
import jp.brainjuice.pokego.business.dao.entity.EvolutionPk;
import jp.brainjuice.pokego.business.dao.entity.GoPokedex;
import jp.brainjuice.pokego.business.service.utils.PokemonEditUtils;
import jp.brainjuice.pokego.business.service.utils.dto.evo.Hierarchy;
import jp.brainjuice.pokego.utils.exception.PokemonDataInitException;

@Component
class EvolutionInfo {

	private EvolutionRepository evolutionRepository;

	private EvolutionUtility evolutionUtility;

	private GoPokedexRepository goPokedexRepository;

	static final String ROOT = "root";

	EvolutionInfo(
			EvolutionUtility evolutionUtility,
			EvolutionRepository evolutionRepository,
			GoPokedexRepository goPokedexRepository) throws PokemonDataInitException {
		this.evolutionUtility = evolutionUtility;
		this.evolutionRepository = evolutionRepository;
		this.goPokedexRepository = goPokedexRepository;
	}

	/**
	 * 進化前のポケモンを取得する。<br>
	 * 進化前が存在しない場合は、nullを返却する。
	 *
	 * @param pokedexId
	 * @return
	 */
	List<String> getBeforeEvolution(String pokedexId) {

		List<String> bidList = evolutionRepository.findBidById(pokedexId);

		return bidList;
	}

	/**
	 * 進化後のポケモンを取得する。
	 *
	 * @param pokedexId
	 * @return
	 */
	List<String> getAfterEvolution(String pokedexId) {

		List<String> pidList = evolutionRepository.findIdByBid(pokedexId);

		return pidList;
	}

	/**
	 * 進化後のポケモンが存在するか判定する。
	 *
	 * @param pokedexId
	 * @return
	 */
	boolean isAfterEvolution(String pokedexId) {

		return !getAfterEvolution(pokedexId).isEmpty();
	}

	/**
	 * 別の「すがた」を取得する。
	 *
	 * @param pokedexId
	 * @return
	 */
	List<String> getAnotherFormList(String pokedexId) {

		List<String> anoFormList = evolutionRepository.findAnoFormById(pokedexId);

		return anoFormList;

	}

	/**
	 * 進化ツリー上の最初のポケモンを取得する。
	 *
	 * ガーメイルのような進化ツリーの場合は、2件以上になる。
	 *
	 * @param pokedexId
	 * @return
	 */
	List<String> getRoot(String pokedexId) {

		List<String> rootList = evolutionRepository.findRootById(pokedexId);

		return rootList;
	}

	/**
	 * 進化ツリー上の最後のポケモンを取得する。
	 *
	 * @param pokedexId
	 * @return
	 */
	List<String> getLeaf(String pokedexId) {

		List<String> leafList = evolutionRepository.findLeafById(pokedexId);

		return leafList;
	}

	/**
	 * 進化ツリー上の最後のポケモンを取得する。<br>
	 * ※ポケモンGOで進化できるポケモンのみ取得する。
	 *
	 * @param pokedexId
	 * @return
	 */
	List<String> getLeafCanGoEvol(String pokedexId) {

		List<String> leafList = evolutionRepository.findLeafByIdCanGoEvol(pokedexId);

		return leafList;
	}

	/**
	 * 最終進化のポケモンかを判定する。
	 *
	 * @param pokedexId
	 * @return
	 */
	boolean isFinalEvolution(String pokedexId) {

		List<String> leafList = getLeaf(pokedexId);
		// 一致するものがあればtrue => 最終進化のポケモンならtrue
		return leafList.stream().anyMatch(e -> pokedexId.equals(e));
	}


	/**
	 * 進化ツリー上の進化前、進化後のポケモンをすべて取得する。<br>
	 * 引数に設定したポケモンは排除する。
	 *
	 * @param pokedexId
	 * @return
	 */
	List<String> getBfAfEvoList(String pokedexId) {

		List<EvolutionPk> pidList = evolutionRepository.getEvolTreePkById(pokedexId);

		return pidList.stream()
				.filter(pk -> !pokedexId.equals(pk.getPokedexId()))
				.map(EvolutionPk::getPokedexId)
				.collect(Collectors.toList());
	}

//	/**
//	 * 同系統のすべてのポケモンを取得する。
//	 *
//	 * @param pokedexId
//	 * @return
//	 */
//	List<String> getAllInEvoTree(String pokedexId) {
//
//		// 別のすがたを取得 -> 進化前、進化後を取得 -> 別のすがたを取得 -> Setに変換 -> Listに変換。
//		return Stream.concat(getAnotherFormList(pokedexId).stream(), Stream.of(pokedexId))
//				.flatMap(pid -> Stream.concat(getBfAfEvoList(pid).stream(), Stream.of(pid)))
//				.flatMap(pid -> Stream.concat(getAnotherFormList(pid).stream(), Stream.of(pid)))
//				.collect(Collectors.toSet()).stream()
//				.collect(Collectors.toList());
//
//	}

	/**
	 * 1系統におけるすべてのポケモンを取得する。
	 * @param pokedexId
	 * @return
	 */
	List<Evolution> getLineageList(String pokedexId) {

		return evolutionRepository.getLineageById(pokedexId);
	}

	/**
	 * 同系統のポケモンにおける一意の図鑑№を取得する。<br>
	 * ※「同系統」とは、ニャイキングの場合、全リージョンのニャース、ペルシアン、ニャイキングを指す。
	 *
	 * @param pokedexId
	 * @return
	 */
	int basePokedexNo(String pokedexId) {
		String pidList = getRoot(pokedexId).get(0);
		// 第一形態のポケモンの図鑑Noを一意の図鑑Noとする。
		return PokemonEditUtils.getPokedexNo(pidList);
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

		List<Evolution> evolList = evolutionRepository.getEvolTreeById(pokedexId);

		List<GoPokedex> gpList = goPokedexRepository.findAllById(
				evolList.stream()
				.map(Evolution::getPokedexId)
				.collect(Collectors.toList()));

		return getEvoTrees(evolList, gpList);
	}

	/**
	 * Hierarchyのリスト（進化ツリー）を取得する。<br>
	 * 引数には、ツリー上のすべてのEvolutionとそれに対応するすべてのGoPokedexを渡すこと。
	 * 異なるツリーのポケモンを複数指定しても問題ない。
	 *
	 * @param evolList
	 * @param gpList
	 * @return
	 */
	List<List<List<Hierarchy>>> getEvoTrees(List<Evolution> evolList, List<GoPokedex> gpList) {

		List<List<List<Hierarchy>>> treeList = null;

		List<EvolutionPk> pkList = evolList.stream().map(EvolutionPk::new).toList();

		List<Hierarchy> hieList = evolList.stream()
				.map(evol -> {
					GoPokedex goPokedex = gpList.stream()
							.filter(gp -> evol.getPokedexId().equals(gp.getPokedexId()))
							.findAny().orElseThrow();

					return new Hierarchy(
							0, // x軸は一旦0で初期化
							getY(evol.getPokedexId(), pkList), // 第何形態かを取得
							0, // x軸の距離も一旦0で初期化
							evol.getPokedexId(),
							evol.getBeforePokedexId(),
							evolutionUtility.getCosts(evol, goPokedex.isImplFlg()),
							evolutionUtility.canGoEvo(evol));
				})
				.toList();

		// 直列のHierarchyをList<List<List<Hierarchy>>>に変換する。
		treeList = splitTree(hieList);

		// HierarchyのX軸、Distを更新
		treeList.forEach(yList -> setX(yList));


		return treeList;
	}

	/**
	 * 直列のHierarchyをList<List<List<Hierarchy>>>に変換する。<br>
	 *
	 *
	 * @param hieList
	 * @return 一番外側のListは、ガーメイルの考慮（ツリーが複数になる場合の考慮）。その内側のListはY軸、さらに内側はX軸
	 */
	private List<List<List<Hierarchy>>> splitTree(List<Hierarchy> hieList) {

		// 第一形態のリスト
		List<Hierarchy> rootList = hieList.stream()
				.filter(hie -> hie.getY() == 1)
				.collect(Collectors.toList());

		// 第二形態以降のリスト
		Map<Integer, List<Hierarchy>> nodeLeafMap = hieList.stream()
				.filter(hie -> hie.getY() != 1)
				.collect(Collectors.groupingBy(Hierarchy::getY));

		List<List<List<Hierarchy>>> treeList = rootList.stream()
				.map(rootHie -> {
					List<List<Hierarchy>> yList = new ArrayList<>();

					// 第一形態のHierarchyをまず追加。
					yList.add(List.of(rootHie));

					if (nodeLeafMap.size() != 0) {
						// 第二形態以降が存在する場合

						// 第一形態のpokedexIdをListに追加し、進化前のpokedexIdを持つリストを初期化。
						List<String> bfPidList = List.of(rootHie.getId());
						for (int i = 2; i <= nodeLeafMap.size() + 1; i++) {
							// 第二形態以降を順番にループ
							List<Hierarchy> xList = new ArrayList<>();
							for (Hierarchy hie: nodeLeafMap.get(i)) {
								if (bfPidList.contains(hie.getBid())) {
									// 進化後のポケモンだった場合、リストに追加。
									xList.add(hie);
								}
							}
							yList.add(xList);

							// 進化前のpokedexIdを持つリストを次のループ用に更新
							bfPidList = xList.stream()
									.map(Hierarchy::getId)
									.collect(Collectors.toList());
						}
					}
					return yList;
				})
				.collect(Collectors.toList());

		return treeList;
	}

	/**
	 * 第何形態か（Y軸が何番目か）を取得する。
	 *
	 * @param pid
	 * @param pkList
	 * @return
	 */
	private int getY(String pid, List<EvolutionPk> pkList) {
		
		if (pkList.stream()
				.filter(pk -> PokemonEditUtils.isMega(pk.getPokedexId()))
				.anyMatch(e -> true)) {
			// メガ進化に進化前、進化後が存在することはない。
			return 1;
		}

		return incrStageCallRecursively(pid, pkList, 0);
	}

	/**
	 * 第一引数に指定した図鑑IDのポケモンが第何形態かを取得する。
	 *
	 * @param pid
	 * @param pkList
	 * @param stage 0を指定
	 * @return
	 */
	private int incrStageCallRecursively(String pid, List<EvolutionPk> pkList, int stage) {

		int retStage = stage + 1;

		// 進化前のポケモンのpidを取得する。
		String bfPid = pkList.stream()
				.filter(pk -> pk.getPokedexId().equals(pid))
				.map(EvolutionPk::getBeforePokedexId)
				.findAny().orElseThrow();

		if (!"root".equals(bfPid)) {
			// 再帰呼び出しする。
			retStage = incrStageCallRecursively(bfPid, pkList, retStage);
		}

		return retStage;
	}

	/**
	 * X軸と進化前のX軸との距離をセットする。
	 *
	 * @param yList
	 */
	private void setX(List<List<Hierarchy>> yList) {

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

}
