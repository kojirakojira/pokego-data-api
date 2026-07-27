package jp.brainjuice.pokego.business.service.search.moves;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;

import jp.brainjuice.pokego.business.constant.Type.TypeEnum;
import jp.brainjuice.pokego.business.service.search.utils.MovesUtils;
import jp.brainjuice.pokego.dao.jpa.entity.FastAttack;
import jp.brainjuice.pokego.dao.jpa.entity.PokemonFastAttack;

/**
 * めざめるパワーのタイプを変換する。<br>
 * めざめるパワーはノーマルタイプの扱いだが、実際にポケモンが覚える時は、<br>
 * ノーマル、フェアリー以外のいずれかのタイプとして覚える。
 */
@Service
public class HiddenPowerConverter {

	/**
	 * めざめるパワー変換後の技IDを変換前の技IDに戻す。<br>
	 * めざめるパワーでない場合は渡された技IDをそのまま返却する。
	 * @param moveId
	 * @return
	 */
	public String resetMoveId(String moveId) {
		if (!MovesUtils.HIDDEN_POWER_RESERVED_WORD_9.equals(moveId.substring(4, 5))) {
			return moveId;
		}
		return MovesUtils.HIDDEN_POWER_MOVE_ID;
	}

	/**
	 * FastAttackのリストにめざめるパワーが存在する場合、タイプをノーマルから、ノーマル、フェアリー以外のタイプに変換する。<br>
	 * moveIdは専用のIDを生成する。<br>
	 * 引数のListに複数めざめるパワーが存在する場合、バグになる可能性があるため注意すること。
	 *
	 * @param fastAttackList
	 * @return
	 */
	public List<FastAttack> convertFastAttackList(List<FastAttack> fastAttackList) {

		Optional<FastAttack> hiddenPowerOp = fastAttackList.stream()
				.filter(ca -> MovesUtils.HIDDEN_POWER_MOVE_ID.equals(ca.getMoveId()))
				.findFirst();

		if (!hiddenPowerOp.isPresent()) {
			// めざめるパワーが存在しない場合
			return fastAttackList;
		}

		// 変換後のめざめるパワー(FastAttack)の生成
		FastAttack hiddenPower = hiddenPowerOp.orElseThrow();
		List<FastAttack> convertedHiddenPowerList = createHiddenPowerFaList(hiddenPower);

		// 変換する。並び順を考慮し、めざめるパワー（ノーマル）が元々あった場所に変換後のFastAttackを設定する。
		List<FastAttack> convertedFastAttackList = fastAttackList.stream()
				.flatMap(ca -> {
					if (ca == hiddenPower) { // バグになりうる比較方法
						return convertedHiddenPowerList.stream();
					} else {
						return Stream.of(ca);
					}
				})
				.toList();

		return convertedFastAttackList;
	}

	/**
	 * PokemonFastAttackのリストにめざめるパワーが存在する場合、タイプをノーマルから、ノーマル、フェアリー以外のタイプに変換する。
	 * 引数のListに複数めざめるパワーが存在する場合、バグになる可能性があるため注意すること。
	 *
	 * @param pfaList
	 * @return
	 */
	public List<PokemonFastAttack> convertPokemonFastAttackList(List<PokemonFastAttack> pfaList) {

		if (!pfaList.stream()
				.filter(fa -> MovesUtils.HIDDEN_POWER_MOVE_ID.equals(fa.getMoveId()))
				.anyMatch(e -> true)) {
			// めざめるパワーが存在しない場合
			return pfaList;
		}

		// 変換後のタイプの全部のmoveIdの一覧
		List<String> convertedMoveIdList;
		{
			String moveIdPrefix = MovesUtils.HIDDEN_POWER_MOVE_ID.substring(0, 4);
			AtomicInteger counter = new AtomicInteger();
			convertedMoveIdList = Arrays.stream(TypeEnum.values())
					.filter(te -> TypeEnum.normal != te && TypeEnum.fairy != te)
					.map(te -> convertMoveId(moveIdPrefix, counter.incrementAndGet()))
					.toList();
		}
		// FastAttackがある場合は、めざめるパワーのFastAttackを作成する。
		Map<String, FastAttack> convertedHiddenPowerMap = pfaList.stream()
				.filter(pfa -> MovesUtils.HIDDEN_POWER_MOVE_ID.equals(pfa.getMoveId())) // めざめるパワーに絞り込む
				.filter(pfa -> pfa.getFastAttack() != null) // FastAttackがある場合のみ
				.findAny() // 1件で十分
				.stream()
				.flatMap(pfa -> createHiddenPowerFaList(pfa.getFastAttack()).stream()) // 変換後のめざめるパワーに変換
				.collect(Collectors.toMap(FastAttack::getMoveId, Function.identity()));

		// 変換する。並び順を考慮し、めざめるパワー（ノーマル）が元々あった場所に変換後のFastAttackを設定する。
		List<PokemonFastAttack> convertedFastAttackList = pfaList.stream()
				.flatMap(pfa -> {
					if (MovesUtils.HIDDEN_POWER_MOVE_ID.equals(pfa.getMoveId())) {
						return convertedMoveIdList.stream()
								.map(mid -> {
									PokemonFastAttack pfaCloneObj = pfa.clone();
									if (pfaCloneObj.getFastAttack() != null) {
										// FastAttackが存在する場合はセットする。
										FastAttack fa = convertedHiddenPowerMap.get(mid);
										pfaCloneObj.setFastAttack(fa);
									}
									pfaCloneObj.setMoveId(mid);
									return pfaCloneObj;
								});
					} else {
						return Stream.of(pfa);
					}
				})
				.toList();

		return convertedFastAttackList;
	}

	private List<FastAttack> createHiddenPowerFaList(FastAttack hiddenPower) {
		String moveIdPrefix = hiddenPower.getMoveId().substring(0, 4);
		AtomicInteger counter = new AtomicInteger();
		List<FastAttack> convertedHiddenPowerList = Arrays.stream(TypeEnum.values())
				.filter(te -> TypeEnum.normal != te && TypeEnum.fairy != te)
				.map(te -> {
					// ディープコピーの取得（FastAttackのフィールドにオブジェクトを保持するようになった場合、バグになりうるため注意が必要）
					FastAttack hiddenPowerCopy = hiddenPower.clone();
					// MoveIdの生成
					String convertedMoveId = convertMoveId(moveIdPrefix, counter.incrementAndGet());
					hiddenPowerCopy.setMoveId(convertedMoveId);
					hiddenPowerCopy.setType(te);
					// タイプを括弧書きでくっつける
					String name = hiddenPowerCopy.getName() + "(" + te.getJpn() + ")";
					hiddenPowerCopy.setName(name);
					return hiddenPowerCopy;
				})
				.toList();
		return convertedHiddenPowerList;
	}

	private String convertMoveId(String moveIdPrefix, int num) {
		return moveIdPrefix
				+ MovesUtils.HIDDEN_POWER_RESERVED_WORD_9
				+ String.format("%02d", num);
	}
}
