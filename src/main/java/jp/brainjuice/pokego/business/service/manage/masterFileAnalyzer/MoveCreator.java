package jp.brainjuice.pokego.business.service.manage.masterFileAnalyzer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;

import com.ibm.icu.text.MessageFormat;

import jp.brainjuice.pokego.business.constant.Type.TypeEnum;
import jp.brainjuice.pokego.business.service.manage.masterFileAnalyzer.dto.CinematicMoveAll;
import jp.brainjuice.pokego.business.service.manage.masterFileAnalyzer.dto.QuickMoveAll;
import jp.brainjuice.pokego.business.service.manage.masterFileAnalyzer.dto.json.Buffs;
import jp.brainjuice.pokego.business.service.search.utils.MovesUtils.MoveCode;
import jp.brainjuice.pokego.dao.jpa.ChargedAttackRepository;
import jp.brainjuice.pokego.dao.jpa.FastAttackRepository;
import jp.brainjuice.pokego.dao.jpa.entity.ChargedAttack;
import jp.brainjuice.pokego.dao.jpa.entity.FastAttack;
import jp.brainjuice.pokego.utils.BjUtils;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MoveCreator {

	private FastAttackRepository fastAttackRepository;

	private ChargedAttackRepository chargedAttackRepository;

	public MoveCreator(
			FastAttackRepository fastAttackRepository,
			ChargedAttackRepository chargedAttackRepository) {
		this.fastAttackRepository = fastAttackRepository;
		this.chargedAttackRepository = chargedAttackRepository;
	}

	/**
	 * マスタデータから取得した通常技の情報をDBに登録する。
	 *
	 * @param quickMoveList
	 * @param masterLinkMap
	 * @param shouldSaveFastAttack
	 */
	void createAndSaveFastAttack(
			List<QuickMoveAll> quickMoveList,
			Map<String, Object> masterLinkMap,
			boolean shouldSaveFastAttack) {

		log.info("------------通常技の登録 ここから------------");
		// マスタデータから取得
		List<FastAttack> fastAttackFromMdList = createFastAttackList(quickMoveList, masterLinkMap);
		// DBから取得
		List<FastAttack> fastAttackFromDbList = fastAttackRepository.findAll();

		List<FastAttack> saveTargetList = filterSaveTargetFastAttack(fastAttackFromMdList, fastAttackFromDbList);
		if (shouldSaveFastAttack) {
			fastAttackRepository.saveAll(saveTargetList);
			log.info("FastAttackをDBに登録しました。");
		} else {
			log.info("FastAttackのDB登録は実行しませんでした。");
		}
		log.info("------------通常技の登録 ここまで------------");
	}

	/**
	 * マスタデータから取得したスペシャル技の情報をDBに登録する。
	 *
	 * @param cinematicMoveList
	 * @param masterLinkMap
	 * @param shouldSaveChargedAttack
	 */
	void createAndSaveChargedAttack(
			List<CinematicMoveAll> cinematicMoveList,
			Map<String, Object> masterLinkMap,
			boolean shouldSaveChargedAttack) {

		log.info("------------スペシャル技の登録 ここから------------");
		// マスタデータから取得
		List<ChargedAttack> chargedAttackFromMdList = createChargedAttackList(cinematicMoveList, masterLinkMap);
		// DBから取得
		List<ChargedAttack> chargedAttackFromDbList = chargedAttackRepository.findAll();

		List<ChargedAttack> saveTargetList = filterSaveTargetChargedAttack(chargedAttackFromMdList,
				chargedAttackFromDbList);
		if (shouldSaveChargedAttack) {
			chargedAttackRepository.saveAll(saveTargetList);
			log.info("ChargedAttackをDBに登録しました。");
		} else {
			log.info("ChargedAttackのDB登録は実行しませんでした。");
		}
		log.info("------------スペシャル技の登録 ここまで------------");
	}

	/**
	 * マスタデータから通常技のリストを生成する
	 *
	 * @param quickMoveAllList
	 * @param masterLinkMap
	 * @return
	 */
	private List<FastAttack> createFastAttackList(List<QuickMoveAll> quickMoveAllList,
			Map<String, Object> masterLinkMap) {

		Map<TypeEnum, List<QuickMoveAll>> typeQuickMoveAllMap = quickMoveAllList.stream()
				.collect(Collectors.groupingBy(qma -> qma.getType()));

		// moveIdを生成しキーとする。
		Map<String, QuickMoveAll> moveIdMap = typeQuickMoveAllMap.entrySet().stream()
				.flatMap(entry -> {
					List<QuickMoveAll> sortedQmaList = entry.getValue().stream()
							.sorted((o1, o2) -> {
								// movementNoの先頭のVを抜いて並び替える（念のため）
								return Integer.valueOf(o1.getMovementNo().substring(1)).intValue()
										- Integer.valueOf(o2.getMovementNo().substring(1)).intValue();
							})
							.toList();
					List<Map.Entry<String, QuickMoveAll>> moveIdEntryList = new ArrayList<>();
					for (int i = 0; i < sortedQmaList.size(); i++) {
						StringBuilder sb = new StringBuilder();
						// タイプの先頭三文字を大文字にした文字列をIDの頭につける
						sb.append(entry.getKey().name().substring(0, 3).toUpperCase());
						// 通常技は1
						sb.append(MoveCode.fast_attack.getCode());
						// タイプごとに3桁の連番
						sb.append(String.format("%03d", i + 1));
						Map.Entry<String, QuickMoveAll> moveIdEntry = Map.entry(sb.toString(), sortedQmaList.get(i));
						moveIdEntryList.add(moveIdEntry);
					}
					return moveIdEntryList.stream();
				})
				.collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue()));

		@SuppressWarnings("unchecked")
		Map<String, String> masterLinkQuickMoveMap = ((Map<String, Map<String, String>>) masterLinkMap
				.get(MasterLinkDataKey.moves.name()))
				.get(MasterLinkDataKey.quick_moves.name());

		// FastAttackに変換して返却
		return moveIdMap.entrySet().stream()
				.map(entry -> {
					QuickMoveAll qma = entry.getValue();
					double dps = BjUtils.round(qma.getGymRaid().getPower(),
							((double) qma.getGymRaid().getDurationMs()) / 1000.0, 3);
					double eps = BjUtils.round(qma.getGymRaid().getEnergyDelta(),
							((double) qma.getGymRaid().getDurationMs() / 1000.0), 3);
					int turns = qma.getPvp().getDurationTurns() + 1; // durationTurnsの項目が存在しない場合は、マスタデータ上は0が入る。（int型だから？）
					double dpt = BjUtils.round(qma.getPvp().getPower(), (double) turns, 3);
					double ept = BjUtils.round(qma.getPvp().getEnergyDelta(), (double) turns, 3);
					return new FastAttack(
							entry.getKey(), // moveId
							masterLinkQuickMoveMap.get(qma.getMovementId()), // name(日本語名)
							qma.getMovementId(), // uniqueId
							qma.getMovementNo(), // movementNo
							qma.getType(), // タイプ（英語名）
							qma.getGymRaid().getPower(), // gymPower
							qma.getGymRaid().getEnergyDelta(), // gymEnergyIncrAmount
							dps, // dps
							eps, // eps
							qma.getGymRaid().getDamageWindowStartMs(), // damageMs
							qma.getGymRaid().getDurationMs(), // totalMs
							qma.getPvp().getPower(), // pvpPower
							qma.getPvp().getEnergyDelta(), // pvpEnergyIncrAmount
							turns, // turns
							dpt, // dpt
							ept); // ept
				})
				.toList();
	}

	/**
	 * 通常技のうち、DBの更新が必要な技に絞り込む
	 * 
	 * @param fastAttackFromMdList
	 * @param fastAttackFromDbList
	 * @return
	 */
	private List<FastAttack> filterSaveTargetFastAttack(List<FastAttack> fastAttackFromMdList,
			List<FastAttack> fastAttackFromDbList) {

		Function<FastAttack, String> createIdFunc = (fa) -> {
			StringBuilder sb = new StringBuilder();
			sb.append(fa.getMovementNo());
			sb.append('_');
			sb.append(fa.getUniqueId());
			return sb.toString();
		};

		Map<String, FastAttack> fromMdMap = fastAttackFromMdList.stream()
				.map(fadb -> Map.entry(createIdFunc.apply(fadb), fadb))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
		Map<String, FastAttack> fromDbMap = fastAttackFromDbList.stream()
				.map(fadb -> Map.entry(createIdFunc.apply(fadb), fadb))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

		log.info(MessageFormat.format("通常技マスタデータ件数: {0}件, DB件数: {1}件", fastAttackFromMdList.size(),
				fastAttackFromDbList.size()));
		log.info("------------【通常技】マスタデータにあるが、DBにないやつ ここから------------");
		// マスタデータ -> DBの比較
		List<FastAttack> mdYesDbNoList = fromMdMap.entrySet().stream()
				.filter(entry -> !fromDbMap.containsKey(entry.getKey())) // マスタデータにある。DBにない
				.sorted((o1, o2) -> o1.getValue().getMoveId().compareTo(o2.getValue().getMoveId()))
				.map(Map.Entry::getValue)
				.sorted((o1, o2) -> o1.getMoveId().compareTo(o2.getMoveId()))
				.toList();
		mdYesDbNoList.stream().forEach(fa -> log.info(fa.toString()));
		log.info("------------【通常技】マスタデータにあるが、DBにないやつ ここまで------------");
		log.info("------------【通常技】DBにあるが、マスタデータにないやつ ここから------------");
		// マスタデータ -> DBの比較
		List<FastAttack> mdNoDbYesList = fromDbMap.entrySet().stream()
				.filter(entry -> !fromMdMap.containsKey(entry.getKey())) // DBにある。マスタデータにない
				.map(Map.Entry::getValue)
				.sorted((o1, o2) -> o1.getMoveId().compareTo(o2.getMoveId()))
				.toList();
		mdNoDbYesList.stream().forEach(fa -> log.info(fa.toString()));
		log.info("------------【通常技】DBにあるが、マスタデータにないやつ ここまで------------");
		log.info("------------【通常技】DBとマスタデータで値が異なる ここから------------");
		Map<String, FastAttack> differentMap = fromMdMap.entrySet().stream()
				.filter(entry -> !mdYesDbNoList.contains(entry.getValue())) // 「マスタデータにあってDBにないやつリスト」にない(=マスタデータ、DB両方にある)やつに絞り込む
				.filter(entry -> {
					FastAttack mdfa = entry.getValue();
					FastAttack dbfa = fromDbMap.get(entry.getKey());
					boolean different = false;
					if (!Objects.equals(mdfa.getMoveId(), dbfa.getMoveId())
							|| !Objects.equals(mdfa.getGymPower(), dbfa.getGymPower())
							|| !Objects.equals(mdfa.getGymEnergyIncrAmount(), dbfa.getGymEnergyIncrAmount())
							|| !Objects.equals(mdfa.getDamageMs(), dbfa.getDamageMs())
							|| !Objects.equals(mdfa.getTotalMs(), dbfa.getTotalMs())
							|| !Objects.equals(mdfa.getPvpPower(), dbfa.getPvpPower())
							|| !Objects.equals(mdfa.getPvpEnergyIncrAmount(), dbfa.getPvpEnergyIncrAmount())
							|| !Objects.equals(mdfa.getTurns(), dbfa.getTurns())) {
						different = true;
					}
					return different;
				})
				.collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue()));

		differentMap.entrySet().stream()
				.sorted((o1, o2) -> o1.getKey().compareTo(o2.getKey()))
				.forEach(entry -> {
					FastAttack mdfa = entry.getValue();
					FastAttack dbfa = fromDbMap.get(entry.getKey());
					log.info(entry.getKey() + "\t[after]\t" + mdfa.toString());
					log.info(entry.getKey() + "\t[before]\t" + dbfa.toString());
				});
		log.info("------------【通常技】DBとマスタデータで値が異なる ここまで------------");

		return Stream.concat(
				mdYesDbNoList.stream(),
				differentMap.entrySet().stream().map(Map.Entry::getValue))
				.toList();
	}

	/**
	 * マスタデータからスペシャル技のリストを生成する
	 *
	 * @param cinematicMoveAllList
	 * @param masterLinkMap
	 * @return
	 */
	private List<ChargedAttack> createChargedAttackList(List<CinematicMoveAll> cinematicMoveAllList,
			Map<String, Object> masterLinkMap) {

		Map<TypeEnum, List<CinematicMoveAll>> typeCinematicMoveAllMap = cinematicMoveAllList.stream()
				.collect(Collectors.groupingBy(cma -> cma.getType()));

		// moveIdを生成しキーとする。
		Map<String, CinematicMoveAll> moveIdMap = typeCinematicMoveAllMap.entrySet().stream()
				.flatMap(entry -> {
					List<CinematicMoveAll> sortedQmaList = entry.getValue().stream()
							.sorted((o1, o2) -> {
								// movementNoの先頭のVを抜いて並び替える（念のため）
								return Integer.valueOf(o1.getMovementNo().substring(1)).intValue()
										- Integer.valueOf(o2.getMovementNo().substring(1)).intValue();
							})
							.toList();
					List<Map.Entry<String, CinematicMoveAll>> moveIdEntryList = new ArrayList<>();
					for (int i = 0; i < sortedQmaList.size(); i++) {
						StringBuilder sb = new StringBuilder();
						// タイプの先頭三文字を大文字にした文字列をIDの頭につける
						sb.append(entry.getKey().name().substring(0, 3).toUpperCase());
						// スペシャル技は2
						sb.append(MoveCode.charged_attack.getCode());
						// タイプごとに3桁の連番
						sb.append(String.format("%03d", i + 1));
						Map.Entry<String, CinematicMoveAll> moveIdEntry = Map.entry(sb.toString(),
								sortedQmaList.get(i));
						moveIdEntryList.add(moveIdEntry);
					}
					return moveIdEntryList.stream();
				})
				.collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue()));

		@SuppressWarnings("unchecked")
		Map<String, String> masterLinkCinematicMoveMap = ((Map<String, Map<String, String>>) masterLinkMap
				.get(MasterLinkDataKey.moves.name()))
				.get(MasterLinkDataKey.cinematic_moves.name());

		// ChargedAttackに変換して返却
		return moveIdMap.entrySet().stream()
				.map(entry -> {
					CinematicMoveAll cma = entry.getValue();
					String name = masterLinkCinematicMoveMap.get(cma.getMovementId());
					if (name == null) {
						log.warn(MessageFormat.format(
								"master_link_data.ymlに、movementId:{0}(moveId:{1})に対応する日本語名が設定されていません。",
								cma.getMovementId(), entry.getKey()));
					}
					double dps = BjUtils.round(cma.getGymRaid().getPower(),
							((double) cma.getGymRaid().getDurationMs() / 1000.0), 3);
					int grEenergyDelta = cma.getGymRaid().getEnergyDelta();
					// なぜかわるあがき(STRUGGLE)は、マスタデータ上にジム・レイドのenergyDeltaが存在しない。
					int energyBar = grEenergyDelta == 0 ? 0 : (int) (100 / grEenergyDelta) * -1;
					double dpe = BjUtils.round(cma.getPvp().getPower(), ((double) cma.getPvp().getEnergyDelta() * -1),
							3);
					Optional<Buffs> buffsOp = Optional.ofNullable(cma.getPvp().getBuffs());
					return new ChargedAttack(
							entry.getKey(), // moveId
							name, // name(日本語名)
							cma.getMovementId(), // uniqueId
							cma.getMovementNo(), // movementNo
							cma.getType(), // タイプ（英語名）
							cma.getGymRaid().getPower(), // gymPower
							cma.getGymRaid().getEnergyDelta(), // gymEnergyIncrAmount
							dps, // dps
							energyBar, // energyBar
							cma.getGymRaid().getDamageWindowStartMs(), // damageMs
							cma.getGymRaid().getDurationMs(), // totalMs
							cma.getPvp().getPower(), // pvpPower
							cma.getPvp().getEnergyDelta(), // pvpEnergyIncrAmount
							dpe, // dpe
							buffsOp.map(Buffs::getAttackerAttackStatStageChange).orElse(0),
							buffsOp.map(Buffs::getAttackerDefenseStatStageChange).orElse(0),
							buffsOp.map(Buffs::getTargetAttackStatStageChange).orElse(0),
							buffsOp.map(Buffs::getTargetDefenseStatStageChange).orElse(0),
							buffsOp.map(Buffs::getBuffActivationChance).orElse(0.0));
				})
				.toList();
	}

	/**
	 * スペシャル技のうち、DBの更新が必要な技に絞り込む
	 *
	 * @param chargedAttackFromMdList
	 * @param chargedAttackFromDbList
	 * @param shouldSaveCinematicMove
	 * @return
	 */
	private List<ChargedAttack> filterSaveTargetChargedAttack(List<ChargedAttack> chargedAttackFromMdList,
			List<ChargedAttack> chargedAttackFromDbList) {

		Function<ChargedAttack, String> createIdFunc = (fa) -> {
			StringBuilder sb = new StringBuilder();
			sb.append(fa.getMovementNo());
			sb.append('_');
			sb.append(fa.getUniqueId());
			return sb.toString();
		};

		Map<String, ChargedAttack> fromMdMap = chargedAttackFromMdList.stream()
				.map(fadb -> Map.entry(createIdFunc.apply(fadb), fadb))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
		Map<String, ChargedAttack> fromDbMap = chargedAttackFromDbList.stream()
				.map(fadb -> Map.entry(createIdFunc.apply(fadb), fadb))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

		log.info(MessageFormat.format("スペシャル技マスタデータ件数: {0}件, DB件数: {1}件", chargedAttackFromMdList.size(),
				chargedAttackFromDbList.size()));
		log.info("------------【スペシャル技】マスタデータにあるが、DBにないやつ ここから------------");
		// マスタデータ -> DBの比較
		List<ChargedAttack> mdYesDbNoList = fromMdMap.entrySet().stream()
				.filter(entry -> !fromDbMap.containsKey(entry.getKey())) // マスタデータにある。DBにない
				.sorted((o1, o2) -> o1.getValue().getMoveId().compareTo(o2.getValue().getMoveId()))
				.map(Map.Entry::getValue)
				.sorted((o1, o2) -> o1.getMoveId().compareTo(o2.getMoveId()))
				.toList();
		mdYesDbNoList.stream().forEach(fa -> log.info(fa.toString()));
		log.info("------------【スペシャル技】マスタデータにあるが、DBにないやつ ここまで------------");
		log.info("------------【スペシャル技】DBにあるが、マスタデータにないやつ ここから------------");
		// マスタデータ -> DBの比較
		List<ChargedAttack> mdNoDbYesList = fromDbMap.entrySet().stream()
				.filter(entry -> !fromMdMap.containsKey(entry.getKey())) // DBにある。マスタデータにない
				.map(Map.Entry::getValue)
				.sorted((o1, o2) -> o1.getMoveId().compareTo(o2.getMoveId()))
				.toList();
		mdNoDbYesList.stream().forEach(fa -> log.info(fa.toString()));
		log.info("------------【スペシャル技】DBにあるが、マスタデータにないやつ ここまで------------");
		log.info("------------【スペシャル技】DBとマスタデータで値が異なる ここから------------");
		Map<String, ChargedAttack> differentMap = fromMdMap.entrySet().stream()
				.filter(entry -> !mdYesDbNoList.contains(entry.getValue())) // 「マスタデータにあってDBにないやつリスト」にない(=マスタデータ、DB両方にある)やつに絞り込む
				.filter(entry -> {
					ChargedAttack mdca = entry.getValue();
					ChargedAttack dbca = fromDbMap.get(entry.getKey());
					boolean different = false;
					if (!Objects.equals(mdca.getMoveId(), dbca.getMoveId())
							|| !Objects.equals(mdca.getGymPower(), dbca.getGymPower())
							|| !Objects.equals(mdca.getGymEnergyIncrAmount(), dbca.getGymEnergyIncrAmount())
							|| !Objects.equals(mdca.getDamageMs(), dbca.getDamageMs())
							|| !Objects.equals(mdca.getTotalMs(), dbca.getTotalMs())
							|| !Objects.equals(mdca.getPvpPower(), dbca.getPvpPower())
							|| !Objects.equals(mdca.getPvpEnergyIncrAmount(), dbca.getPvpEnergyIncrAmount())
							|| !Objects.equals(mdca.getOwnAttackBuff(), dbca.getOwnAttackBuff())
							|| !Objects.equals(mdca.getOwnDefenseBuff(), dbca.getOwnDefenseBuff())
							|| !Objects.equals(mdca.getOppAttackBuff(), dbca.getOppAttackBuff())
							|| !Objects.equals(mdca.getOppDefenseBuff(), dbca.getOppDefenseBuff())) {
						different = true;
					}
					return different;
				})
				.collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue()));

		differentMap.entrySet().stream()
				.sorted((o1, o2) -> o1.getKey().compareTo(o2.getKey()))
				.forEach(entry -> {
					ChargedAttack mdca = entry.getValue();
					ChargedAttack dbca = fromDbMap.get(entry.getKey());
					log.info(entry.getKey() + "\t[after]\t" + mdca.toString());
					log.info(entry.getKey() + "\t[before]\t" + dbca.toString());
				});
		log.info("------------【スペシャル技】DBとマスタデータで値が異なる ここまで------------");

		return Stream.concat(
				mdYesDbNoList.stream(),
				differentMap.entrySet().stream().map(Map.Entry::getValue))
				.toList();

	}
}
