package jp.brainjuice.pokego.business.service.manage.masterFileAnalyzer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.icu.text.MessageFormat;

import jp.brainjuice.pokego.business.constant.Type.TypeEnum;
import jp.brainjuice.pokego.business.service.manage.masterFileAnalyzer.dto.CinematicMoveAll;
import jp.brainjuice.pokego.business.service.manage.masterFileAnalyzer.dto.ParsedMasterData;
import jp.brainjuice.pokego.business.service.manage.masterFileAnalyzer.dto.PokemonMove;
import jp.brainjuice.pokego.business.service.manage.masterFileAnalyzer.dto.PokemonMoveAll;
import jp.brainjuice.pokego.business.service.manage.masterFileAnalyzer.dto.QuickMoveAll;
import jp.brainjuice.pokego.business.service.manage.masterFileAnalyzer.dto.json.CinematicCombatMoveData;
import jp.brainjuice.pokego.business.service.manage.masterFileAnalyzer.dto.json.FormChange;
import jp.brainjuice.pokego.business.service.manage.masterFileAnalyzer.dto.json.MasterRoot;
import jp.brainjuice.pokego.business.service.manage.masterFileAnalyzer.dto.json.Move;
import jp.brainjuice.pokego.business.service.manage.masterFileAnalyzer.dto.json.MoveData;
import jp.brainjuice.pokego.business.service.manage.masterFileAnalyzer.dto.json.PokemonData;
import jp.brainjuice.pokego.business.service.manage.masterFileAnalyzer.dto.json.PokemonStats;
import jp.brainjuice.pokego.business.service.manage.masterFileAnalyzer.dto.json.QuickCombatMoveData;
import jp.brainjuice.pokego.business.service.manage.masterFileAnalyzer.dto.json.Shadow;
import jp.brainjuice.pokego.business.service.search.utils.MovesUtils.MoveCategory;
import jp.brainjuice.pokego.business.service.search.utils.PokemonEditUtils;
import jp.brainjuice.pokego.dao.jpa.GoPokedexRepository;
import jp.brainjuice.pokego.dao.jpa.entity.GoPokedex;
import jp.brainjuice.pokego.utils.BjUtils;
import jp.brainjuice.pokego.utils.exception.PokemonDataException;
import lombok.extern.slf4j.Slf4j;

/**
 * マスタファイルを解析する。
 */
@Service
@Slf4j
public class MasterFileAnalyzerService {

	private ObjectMapper objectMapper;

	private GoPokedexRepository goPokedexRepository;

	private MoveCreator moveCreator;

	private PokemonMoveCreator pokemonMoveCreator;

	private static final String MASTER_LINK_DATA_FILE = "pokemon/master_link_data.yml";

	public MasterFileAnalyzerService(
			ObjectMapper objectMapper,
			GoPokedexRepository goPokedexRepository,
			MoveCreator moveCreator,
			PokemonMoveCreator pokemonMoveCreator) {
		this.objectMapper = objectMapper;
		this.goPokedexRepository = goPokedexRepository;
		this.moveCreator = moveCreator;
		this.pokemonMoveCreator = pokemonMoveCreator;
	}

	public void analyze(
			MultipartFile masterFile,
			boolean isPrintQuickMoves,
			boolean isPrintCinematicMoves,
			boolean isPrintRequestedMoveEachPokemon,
			boolean shouldSaveFastAttack,
			boolean shouldSaveChargedAttack) throws StreamReadException, DatabindException, IOException {

		// マスタデータから必要な情報を抜き出す
		ParsedMasterData parsedMasterData = loadMasterData(masterFile);

		// master_link_data.ymlを読み込む
		Map<String, Object> masterLinkMap = loadMasterLinkData();

		// ぺりずかんの情報を取得
		Map<String, GoPokedex> gpMap = goPokedexRepository.findAll().stream()
				.collect(Collectors.toMap(
						gp -> gp.getPokedexId(),
						gp -> gp));

		// ペリずかん上のポケモンのデータが正しいかチェックする。（技は含まない）
		checkPeriData(parsedMasterData.getPokemonDataList(), gpMap, masterLinkMap);

		// ポケモンが覚える技の整理
		Map<String, PokemonMoveAll> pokemonMoveAllMap = createPokemonMoveAllMap(
				parsedMasterData.getPokemonDataList(),
				parsedMasterData.getAdditionalCinematicMoveMap(),
				masterLinkMap,
				gpMap);

		// 技の一覧を出力する。
		if (isPrintQuickMoves) {
			printQuickMoves(
					parsedMasterData.getQuickMoveList(),
					pokemonMoveAllMap,
					masterLinkMap);
		}

		if (isPrintCinematicMoves) {
			printCinematicMoves(
					parsedMasterData.getCinematicMoveList(),
					pokemonMoveAllMap,
					masterLinkMap);
		}

		// ポケモンごとの技を出力する。
		if (isPrintRequestedMoveEachPokemon) {
			printMovesForEachPokemon(pokemonMoveAllMap, gpMap);
		}

		// マスタデータから通常技のリストを生成し、DBに登録
		moveCreator.createAndSaveFastAttack(parsedMasterData.getQuickMoveList(), masterLinkMap, shouldSaveFastAttack);

		// マスタデータからスペシャル技のリストを生成し、DBに登録
		moveCreator.createAndSaveChargedAttack(parsedMasterData.getCinematicMoveList(), masterLinkMap, shouldSaveChargedAttack);

		// マスタデータからポケモンが覚える通常技のリストを生成し、DBに登録
		pokemonMoveCreator.createAndSaveFastAttack(pokemonMoveAllMap.values(), shouldSaveFastAttack);

		// マスタデータからポケモンが覚えるスペシャル技のリストを生成し、DBに登録
		pokemonMoveCreator.createAndSaveChargedAttack(pokemonMoveAllMap.values(), shouldSaveChargedAttack);

	}

	private ParsedMasterData loadMasterData(MultipartFile masterFile) throws StreamReadException, DatabindException, IOException {

		List<QuickMoveAll> quickMoveList;
		List<CinematicMoveAll> cinematicMoveList;
		List<PokemonData> pokemonDataList;
		// PokemonDataのcinematicMovesに含まれない技。Map<templateId(ポケモン), List<<スペシャル技>>
		Map<String, List<PokemonMove>> additionalCinematicMoveMap;
		{
			List<MasterRoot> jsonData = objectMapper.readValue(masterFile.getInputStream(), new TypeReference<List<MasterRoot>>() {});

			{
				// 技データ
				// templateId命名ルール：ジム・レイド用のデータは接頭辞は付かず、PvP用のデータはCOMBAT_の接頭辞が付く。通常技は後ろに_FASTが付く模様。
				final String COMBAT_MOVE_PATTERN = "(?:COMBAT_)?(V\\d{4}_MOVE_.+)";
				final String COMBAT_PREFIX = "COMBAT_";
				final String FAST_MOVE_SUFFIX = "_FAST";

				List<MasterRoot> moveDataList = jsonData.stream()
						.filter(mr -> mr.getTemplateId().matches(COMBAT_MOVE_PATTERN))
						.toList();
				// 通常技
				{
					List<QuickCombatMoveData> quickGymRaidMoveList = moveDataList.stream()
							.filter(mr -> mr.getTemplateId().startsWith(COMBAT_PREFIX)) // COMBATがつく
							.filter(mr -> mr.getTemplateId().contains(FAST_MOVE_SUFFIX)) // FASTもつく(最後につくとは限らない(_FAST_BLASTOISE))
							.map(mr -> objectMapper.convertValue(mr.getData(), QuickCombatMoveData.class))
							.toList();
					Map<String, Move> quickPvpMap = moveDataList.stream()
							.filter(mr -> !mr.getTemplateId().startsWith(COMBAT_PREFIX)) // COMBATがつかない
							.filter(mr -> mr.getTemplateId().contains(FAST_MOVE_SUFFIX)) // FASTはつく(最後につくとは限らない)
							.map(mr -> objectMapper.convertValue(mr.getData(), MoveData.class))
							.collect(Collectors.toMap(
									mr -> mr.getMoveSettings().getMovementId(),
									mr -> mr.getMoveSettings()));
					quickMoveList = quickGymRaidMoveList.stream()
							.map(qcmd -> {
								String movementId = qcmd.getCombatMove().getUniqueId();
								String movementNo = Pattern.compile("_(V\\d{4})_")
										.matcher(qcmd.getTemplateId())
										.results() // Stream<MatchResult>
										.findFirst()
										.map(matchResult -> matchResult.group(1))
										.orElse("No match found");
								String strType = qcmd.getCombatMove().getType().replaceFirst("POKEMON_TYPE_", "").toLowerCase();
								TypeEnum type = TypeEnum.valueOf(strType);
								return new QuickMoveAll(
										movementId,
										movementNo,
										type,
										quickPvpMap.get(movementId),
										qcmd.getCombatMove());
							})
							.toList();
				}

				// スペシャル技
				{
					List<CinematicCombatMoveData> cinematicGymRaidMoveList = moveDataList.stream()
							.filter(mr -> mr.getTemplateId().startsWith(COMBAT_PREFIX)) // COMBATがつくが…
							.filter(mr -> !mr.getTemplateId().contains(FAST_MOVE_SUFFIX)) // FASTはつかない(最後につくとは限らない)
							.map(mr -> objectMapper.convertValue(mr.getData(), CinematicCombatMoveData.class))
							.toList();
					Map<String, Move> cinematicPvpMap = moveDataList.stream()
							.filter(mr -> !mr.getTemplateId().startsWith(COMBAT_PREFIX)) // COMBATがつかない
							.filter(mr -> !mr.getTemplateId().contains(FAST_MOVE_SUFFIX)) // FASTもつかない(最後につくとは限らない)
							.map(mr -> objectMapper.convertValue(mr.getData(), MoveData.class))
							.collect(Collectors.toMap(
									mr -> mr.getMoveSettings().getMovementId(),
									mr -> mr.getMoveSettings()));
					cinematicMoveList = cinematicGymRaidMoveList.stream()
							.map(ccmd -> {
								String movementId = ccmd.getCombatMove().getUniqueId();
								String movementNo = Pattern.compile("_(V\\d{4})_")
										.matcher(ccmd.getTemplateId())
										.results() // Stream<MatchResult>
										.findFirst()
										.map(matchResult -> matchResult.group(1))
										.orElse("No match found");
								String strType = ccmd.getCombatMove().getType().replaceFirst("POKEMON_TYPE_", "").toLowerCase();
								TypeEnum type = TypeEnum.valueOf(strType);
								return new CinematicMoveAll(
										movementId,
										movementNo,
										type,
										cinematicPvpMap.get(movementId),
										ccmd.getCombatMove());
							})
							.toList();
				}
			}

			// ポケモンデータ
			{
				final String POKEMON_PATTERN = "V\\d{4}_POKEMON_.+";
				// テンプレートID除外パターン
				final String EXCLUDE_PATTERN1 = ".*_HOME_FORM_REVERSION";
				final String EXCLUDE_PATTERN2 = "V0487_POKEMON_GIRATINA_HOME_REVERSION";

				pokemonDataList = jsonData.stream()
						.filter(mr -> mr.getTemplateId().matches(POKEMON_PATTERN))
						.filter(mr -> !mr.getTemplateId().matches(EXCLUDE_PATTERN1))
						.filter(mr -> !mr.getTemplateId().matches(EXCLUDE_PATTERN2))
						.map(mr -> objectMapper.convertValue(mr.getData(), PokemonData.class))
						.toList();

				additionalCinematicMoveMap = new HashMap<>();
				// シャドウ、リトレーン技を追加
				pokemonDataList.stream()
				.filter(pd -> pd.getPokemonSettings().getShadow() != null)
				.forEach(pd -> {
					String templateId = pd.getTemplateId();
					Shadow shadow = pd.getPokemonSettings().getShadow();
					if (shadow.getShadowChargeMove() != null) {
						PokemonMove pm = new PokemonMove(shadow.getShadowChargeMove(), MoveCategory.shadow, false);
						additionalCinematicMoveMap.computeIfAbsent(templateId, k -> new ArrayList<>()).add(pm);
					}

					if (shadow.getPurifiedChargeMove() != null) {
						PokemonMove pm = new PokemonMove(shadow.getPurifiedChargeMove(), MoveCategory.purified, false);
						additionalCinematicMoveMap.computeIfAbsent(templateId, k -> new ArrayList<>()).add(pm);
					}
				});

				// フォルムチェンジで覚える技を追加
				for (PokemonData pd: pokemonDataList) {
					if (pd.getPokemonSettings().getFormChange() == null) {
						continue;
					}

					for (FormChange fc: pd.getPokemonSettings().getFormChange()) {
						if (fc.getMoveReassignment() == null || fc.getMoveReassignment().getCinematicMoves() == null) {
							continue;
						}

						// フォルムチェンジ後に覚える技のリスト
						List<String> movementIdList = fc.getMoveReassignment().getCinematicMoves().stream()
								.filter(rm -> rm.getReplacementMoves() != null)
								.flatMap(rm -> rm.getReplacementMoves().stream())
								.toList();

						// templateIdが重複する可能性があるため、一旦リストで持つ。
						List<Map.Entry<String, String>> formChangeList = fc.getAvailableForm().stream()
								.flatMap(aForm -> {
									// form -> Map.Entry<form, movementId>に変換
									return movementIdList.stream()
											.map(mid -> Map.entry(aForm, mid));
								}) // return Map.Entry<form, movementId>
								.map(entry -> {
									// Map.Entry<form, movementId> -> Map.Entry<フォルムチェンジ後のポケモンのtemplateId, movementId> の変換
									String templateId = pokemonDataList.stream()
											.filter(pd2 -> entry.getKey().equals(pd2.getPokemonSettings().getForm()))
											.findFirst()
											.orElseThrow()
											.getTemplateId();
									return Map.entry(templateId, entry.getValue());
								})
								.toList();

						formChangeList.stream()
						.forEach(entry -> {
							PokemonMove pm = new PokemonMove(entry.getValue(), MoveCategory.formChange, false);
							additionalCinematicMoveMap.computeIfAbsent(entry.getValue(), k -> new ArrayList<>()).add(pm);
						});
					}
				}

				// 重複を除去する
				additionalCinematicMoveMap.entrySet().stream()
				.forEach(entry -> {
					List<PokemonMove> pm = entry.getValue()
							.stream()
							.distinct()
							.toList();
					entry.setValue(pm);
				});
			}
		}


		ParsedMasterData pmd = new ParsedMasterData();
		pmd.setPokemonDataList(pokemonDataList);
		pmd.setQuickMoveList(quickMoveList);
		pmd.setCinematicMoveList(cinematicMoveList);
		pmd.setAdditionalCinematicMoveMap(additionalCinematicMoveMap);

		return pmd;
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> loadMasterLinkData() {

		Map<String, Object> masterLinkMap = null;
		try {
			masterLinkMap = BjUtils.loadYaml(MASTER_LINK_DATA_FILE, Map.class);
		} catch (IOException e) {
			throw new PokemonDataException("ファイルの読み込みに失敗しました。", e);
		}

		return masterLinkMap;
	}

	/**
	 * ペリずかん上のデータに誤りがないかチェックする。
	 */
	private void checkPeriData(List<PokemonData> pokemonDataList, Map<String, GoPokedex> gpMap, Map<String, Object> masterLinkMap) {

		@SuppressWarnings("unchecked")
		final Map<String, String> masterLinkPokemonMap = ((Map<String, String>) masterLinkMap.get(MasterLinkDataKey.pokemon.name()));

		log.info("------------ポケモン - テンプレートID一覧ここから------------");
		pokemonDataList.forEach(pd -> log.info(pd.getTemplateId()));
		log.info("------------ポケモン - テンプレートID一覧ここまで------------");

		log.info("------------ポケモン - ステータスチェックここから------------");
		log.info("①マスタデータにあって、master_link_data.ymlにないやつ ここから");
		{
			List<String> notDefinedPokemonTemplateIdList = pokemonDataList.stream()
					.filter(pd -> !masterLinkPokemonMap.containsKey(pd.getTemplateId()))
					.map(pd -> pd.getTemplateId())
					.toList();
			if (!notDefinedPokemonTemplateIdList.isEmpty()) {
				notDefinedPokemonTemplateIdList.forEach(log::warn);
				throw new PokemonDataException("master_link_data.ymlに存在しないポケモンが存在します。");
			}
		}
		log.info("①マスタデータにあって、master_link_data.ymlにないやつ ここまで");

		Map<String, String> masterLinkPokemonEmptyRemovedMap = masterLinkPokemonMap.entrySet().stream()
				.filter(entry -> !StringUtils.isEmpty(entry.getValue()))
				.collect(Collectors.toMap(
						Map.Entry::getKey,
						Map.Entry::getValue));

		log.info("②ステータス検査対象外 ここから");
		log.info("(go_pokedexにあって、master_link_data.ymlにないやつ。メガシンカ、ゲンシカイキを除く)");
		{
			Set<String> pokedexIdSet = gpMap.keySet();
			Set<String> masterLinkPidSet = masterLinkPokemonEmptyRemovedMap.entrySet().stream()
					.filter(entry -> !StringUtils.isEmpty(entry.getValue()))
					.map(Map.Entry::getValue)
					.collect(Collectors.toSet());

			List<String> existsGpPidList = pokedexIdSet.stream()
					.filter(pid -> !masterLinkPidSet.contains(pid))
					.filter(pid -> gpMap.get(pid).getPreMegaPokedexId() == null) // メガシンカ、ゲンシカイキを除く
					.sorted(PokemonEditUtils.getPokedexIdComparator())
					.toList();
			existsGpPidList.stream().forEach(pid -> {
				GoPokedex gp = gpMap.get(pid);
				log.info("\t" + PokemonEditUtils.appendRemarks(gp));
			});
		}
		log.info("②ステータス検査対象外 ここまで");
		log.info("③ステータス検査 ここから");
		for (PokemonData pd: pokemonDataList) {
			if (!masterLinkPokemonEmptyRemovedMap.containsKey(pd.getTemplateId())) {
				continue;
			}

			PokemonStats ps = pd.getPokemonSettings().getStats();
			GoPokedex gp = gpMap.get(masterLinkPokemonEmptyRemovedMap.get(pd.getTemplateId()));

			if (ps.getBaseStamina() == 0 || ps.getBaseAttack() == 0 || ps.getBaseDefense() == 0) {
				// statsが存在しない場合
				log.warn(MessageFormat.format("\tstatsなし => {0}(pokedexId: {1}, templateId: {2})",
						PokemonEditUtils.appendRemarks(gp),
						gp.getPokedexId(),
						pd.getTemplateId()));
				continue;
			}

			if (ps.getBaseStamina() == gp.getHp() &&
					ps.getBaseAttack() == gp.getAttack() &&
					ps.getBaseDefense() == gp.getDefense()) {
				// 一致している場合
				continue;
			}

			log.warn(MessageFormat.format("\tステータスに誤りあり => {0}(pokedexId: {1}, templateId: {2})",
					PokemonEditUtils.appendRemarks(gp),
					gp.getPokedexId(),
					pd.getTemplateId()));
		}
		log.info("③ステータス検査 ここまで");

		log.info("------------ポケモン - ステータスチェックここまで------------");
	}

	/**
	 *
	 * @param pokemonDataList
	 * @param additionalCinematicMoveMap
	 * @param masterLinkMap
	 * @param gpMap Map<pokedexId, PokemonMoveAll>
	 * @return
	 */
	private Map<String, PokemonMoveAll> createPokemonMoveAllMap(
			List<PokemonData> pokemonDataList,
			Map<String, List<PokemonMove>> additionalCinematicMoveMap,
			Map<String, Object> masterLinkMap,
			Map<String, GoPokedex> gpMap) {

		// 返却値の初期化
		Map<String, PokemonMoveAll> pokemonMoveAllMap = gpMap.entrySet().stream()
				.filter(entry -> StringUtils.isEmpty(entry.getValue().getPreMegaPokedexId())) // メガシンカを除く
				.collect(Collectors.toMap(
						Map.Entry::getKey,
						entry -> new PokemonMoveAll(entry.getKey(), null))); // ValueのPokemonMoveAllはpokedexIdだけをセットして初期化

		@SuppressWarnings("unchecked")
		Map<String, String> masterLinkPokemonMap = (Map<String, String>) masterLinkMap.get(MasterLinkDataKey.pokemon.name());

		{
			// 第1、第2引数の値からPokemMoveのインスタンスを生成し、第3引数のmoveListに追加する関数
			BiFunction<List<String>, MoveCategory, Consumer<List<PokemonMove>>> pokemonMoveListAddFunc = (movementIdList, moveCategory) -> (moveList) -> {
				if (movementIdList != null) {
					List<PokemonMove> pmList = movementIdList.stream()
							.map(mid -> new PokemonMove(mid, moveCategory, false))
							.toList();
					moveList.addAll(pmList);
				}
			};

			for (PokemonData pd: pokemonDataList) {
				if (masterLinkPokemonMap.get(pd.getTemplateId()) == null) {
					continue;
				}

				if (pd.getPokemonSettings() == null) {
					// pokemonSettingsが存在しない場合スキップ(多分そんなパターンはない)
					log.warn(pd.getTemplateId() + " pokemonSettings is null");
					continue;
				}

				String pokedexId = gpMap.get(masterLinkPokemonMap.get(pd.getTemplateId())).getPokedexId(); // 念の為GoPokedexからpokedexIdを取得
				PokemonMoveAll pma = pokemonMoveAllMap.get(pokedexId);


				// 通常技
				if (pd.getPokemonSettings().getQuickMoves() == null) {
					log.warn(pd.getTemplateId() + " quickMoves is null");
				}
				// 通常技
				pokemonMoveListAddFunc.apply(pd.getPokemonSettings().getQuickMoves(), MoveCategory.normal).accept(pma.getQuickMoveList());
				// 通常技（レガシー）
				pokemonMoveListAddFunc.apply(pd.getPokemonSettings().getEliteQuickMove(), MoveCategory.elite).accept(pma.getQuickMoveList());

				// スペシャル技
				if (pd.getPokemonSettings().getCinematicMoves() == null) {
					log.warn(pd.getTemplateId() + " cinematicMoves is null");
				}
				// スペシャル技
				pokemonMoveListAddFunc.apply(pd.getPokemonSettings().getCinematicMoves(), MoveCategory.normal).accept(pma.getCinematicMoveList());
				// スペシャル技（レガシー）
				pokemonMoveListAddFunc.apply(pd.getPokemonSettings().getEliteCinematicMove(), MoveCategory.elite).accept(pma.getCinematicMoveList());
				// スペシャル技（その他（ガリョウテンセイだけの認識））
				pokemonMoveListAddFunc.apply(pd.getPokemonSettings().getNonTmCinematicMoves(), MoveCategory.other).accept(pma.getCinematicMoveList());
			}
		}

		// シャドウ、リトレーン、フォルムチェンジ等の変則的なスペシャル技の追加
		for (Map.Entry<String, List<PokemonMove>> entry: additionalCinematicMoveMap.entrySet()) {

			String templateId = entry.getKey();

			if (StringUtils.isEmpty(masterLinkPokemonMap.get(templateId))) {
				// ぺりずかんとして使用しないtemplateIdの場合はスキップ
				continue;
			}
			// additionalCinematicMoveMapはキーがポケモンのtemplateIdのため、pokedexIdに変換する必要がある。
			String pokedexId = gpMap.get(masterLinkPokemonMap.get(templateId)).getPokedexId(); // 念の為GoPokedexからpokedexIdを取得

			PokemonMoveAll pma = pokemonMoveAllMap.get(pokedexId);
			pma.getCinematicMoveList().addAll(entry.getValue());
		}

		// mster_link_data.ymlから取得し調整用の技の追加
		{
			@SuppressWarnings("unchecked")
			Map<String, Map<String, Map<String, String>>> tuneMoveMap = (Map<String, Map<String, Map<String, String>>>) masterLinkMap.get(MasterLinkDataKey.tune_moves.name());

			// Map<pokedexId, List<PokemonMove>>
			Map<String, List<PokemonMove>> tuneMap =
					Stream.concat(
							Optional.ofNullable(tuneMoveMap.get(MasterLinkDataKey.limited_time_learned_cinematic_moves.name()))
							.orElse(Map.of()).entrySet().stream(),
							Optional.ofNullable(tuneMoveMap.get(MasterLinkDataKey.not_defined_cinematic_moves.name()))
							.orElse(Map.of()).entrySet().stream())
					.map(entry -> {
						String pokedexId = entry.getKey();
						String movementId = entry.getValue().get(MasterLinkDataKey.movement_id.name());
						String moveCategory = entry.getValue().get(MasterLinkDataKey.move_category.name());

						return Map.entry(pokedexId, new PokemonMove(movementId, MoveCategory.valueOf(moveCategory), true));
					})
					.collect(Collectors.groupingBy(
							entry -> entry.getKey(),
							Collectors.mapping(
									entry -> entry.getValue(),
									Collectors.toList())));

			for (Map.Entry<String, List<PokemonMove>> entry: tuneMap.entrySet()) {

				PokemonMoveAll pma = pokemonMoveAllMap.get(entry.getKey());
				pma.getCinematicMoveList().addAll(entry.getValue());
			}
		}

		return pokemonMoveAllMap;
	}

	/**
	 * 技の一覧を出力する
	 *
	 * @param parsedMasterData
	 * @param pokemonMoveAllMap
	 */
	private void printQuickMoves(
			List<QuickMoveAll> quickMoveList,
			Map<String, PokemonMoveAll> pokemonMoveAllMap,
			Map<String, Object> masterLinkMap) {

		@SuppressWarnings("unchecked")
		Map<String, Map<String, String>> masterLinkMoveMap = (Map<String, Map<String, String>>) masterLinkMap.get(MasterLinkDataKey.moves.name());

		log.info("------------技 - 通常技一覧一覧ここから------------");
		{
			// ポケモンが覚えてる技
			List<String> canUsedQuickMoveList = pokemonMoveAllMap.entrySet().stream()
					.map(entry -> entry.getValue().getQuickMoveList())
					.flatMap(pmList -> pmList.stream()
							.map(pm -> pm.getMovementId()))
					.sorted()
					.distinct()
					.toList();

			// どのポケモンも覚えていない技
			List<String> cantUsedQuickMoveList = quickMoveList.stream()
					.filter(qma -> !canUsedQuickMoveList.contains(qma.getGymRaid().getMovementId()))
					.map(qma -> qma.getMovementId())
					.toList();

			if (cantUsedQuickMoveList.isEmpty()) {
				log.info("誰も覚えていない技は1件もありませんでした。");
			} else {
				cantUsedQuickMoveList.stream().forEach(mid -> {
					log.info(MessageFormat.format("誰も覚えていない技：movementId:\t{0}", mid));
				});
			}

			Map<String, String> masterLinkQuickMoveMap = masterLinkMoveMap.get(MasterLinkDataKey.quick_moves.name());

			// master_link_data.ymlに定義していない技
			List<String> notDefinedQuickMoveList = quickMoveList.stream()
					.map(qma -> qma.getMovementId())
					.filter(mid -> !masterLinkQuickMoveMap.containsKey(mid))
					.toList();

			if (notDefinedQuickMoveList.isEmpty()) {
				log.info("master_link_data.ymlに定義していない技はありませんでした。");
			} else {
				notDefinedQuickMoveList.stream().forEach(mid -> {
					log.warn(MessageFormat.format("master_link_data.ymlに定義していない技：movementId:\t{0}", mid));
				});
				throw new PokemonDataException("master_link_data.ymlに存在しないスペシャル技が存在します。");
			}

			quickMoveList.stream()
			.filter(qma -> !cantUsedQuickMoveList.contains(qma.getGymRaid().getMovementId())) // 誰も覚えていない技は省く
			.map(qma -> List.of(
					qma.getMovementId(),
					masterLinkQuickMoveMap.get(qma.getMovementId()),
					qma.getType().name(),
					String.valueOf(qma.getGymRaid().getPower()),
					String.valueOf(qma.getPvp().getPower())
					))
			.map(list -> String.join("\t", list))
			.forEach(log::info);
		}
		log.info("------------技 - 通常技一覧一覧ここまで------------");

	}


	/**
	 * 技の一覧を出力する
	 *
	 * @param parsedMasterData
	 * @param pokemonMoveAllMap
	 */
	private void printCinematicMoves(
			List<CinematicMoveAll> cinematicMoveList,
			Map<String, PokemonMoveAll> pokemonMoveAllMap,
			Map<String, Object> masterLinkMap) {

		@SuppressWarnings("unchecked")
		Map<String, Map<String, String>> masterLinkMoveMap = (Map<String, Map<String, String>>) masterLinkMap.get(MasterLinkDataKey.moves.name());

		log.info("------------技 - スペシャル技一覧一覧ここから------------");
		{
			// ポケモンが覚えてる技
			List<String> canUseCinematicMoveList = pokemonMoveAllMap.entrySet().stream()
					.map(entry -> entry.getValue().getCinematicMoveList())
					.flatMap(pmList -> pmList.stream()
							.map(pm -> pm.getMovementId()))
					.sorted()
					.distinct()
					.toList();

			// どのポケモンも覚えていない技
			List<String> cantCinematicMoveList = cinematicMoveList.stream()
					.filter(qma -> {
						String mid = qma.getGymRaid().getMovementId();
						return !canUseCinematicMoveList.contains(mid);
					})
					.map(qma -> qma.getMovementId())
					.toList();

			if (cantCinematicMoveList.isEmpty()) {
				log.info("誰も覚えていない技は1件もありませんでした。");
			} else {
				cantCinematicMoveList.stream().forEach(mid -> {
					log.info(MessageFormat.format("誰も覚えていない技：movementId:\t{0}", mid));
				});
			}

			Map<String, String> masterLinkCinematicMoveMap = masterLinkMoveMap.get(MasterLinkDataKey.cinematic_moves.name());

			// master_link_data.ymlに定義していない技
			List<String> notDefinedCinematicMoveList = cinematicMoveList.stream()
					.map(cma -> cma.getMovementId())
					.filter(mid -> !masterLinkCinematicMoveMap.containsKey(mid))
					.toList();

			if (notDefinedCinematicMoveList.isEmpty()) {
				log.info("master_link_data.ymlに定義していない技はありませんでした。");
			} else {
				notDefinedCinematicMoveList.stream().forEach(mid -> {
					log.warn(MessageFormat.format("master_link_data.ymlに定義していない技：movementId:\t{0}", mid));
				});
				throw new PokemonDataException("master_link_data.ymlに存在しないスペシャル技が存在します。");
			}

			cinematicMoveList.stream()
			.filter(qma -> !cantCinematicMoveList.contains(qma.getGymRaid().getMovementId())) // 誰も覚えていない技は省く
			.map(cma -> List.of(
					cma.getMovementId(),
					masterLinkCinematicMoveMap.get(cma.getMovementId()),
					cma.getType().name(),
					String.valueOf(cma.getGymRaid().getPower()),
					String.valueOf(cma.getPvp().getPower())
					))
			.map(list -> String.join("\t", list))
			.forEach(log::info);
		}
		log.info("------------技 - スペシャル技一覧一覧ここまで------------");
	}

	/**
	 * ポケモンが覚えることができる技の一覧を出力する
	 *
	 * @param pokemonMoveAllMap
	 * @param gpMap
	 */
	private void printMovesForEachPokemon(Map<String, PokemonMoveAll> pokemonMoveAllMap, Map<String, GoPokedex> gpMap) {

		Function<Function<PokemonMoveAll, List<PokemonMove>>, List<String>> createStrMoveListFunc = (moveListGetFunc) -> {
			return pokemonMoveAllMap.entrySet().stream()
					.map(Map.Entry::getValue)
					.sorted((o1, o2) -> PokemonEditUtils.getPokedexIdComparator().compare(o1.getPokedexId(), o2.getPokedexId()))
					.flatMap(pma -> {
						GoPokedex gp = gpMap.get(pma.getPokedexId());
						String name = PokemonEditUtils.appendRemarks(gp);

						return moveListGetFunc.apply(pma).stream()
								.map(pm -> MessageFormat.format("{0}\t{1}\t{2}\t{3}", gp.getPokedexId(), name, pm.getMovementId(), pm.getCategory().name()));
					})
					.toList();
		};
		log.info("------------技 - ポケモンと技 ここから------------");
		log.info("-----①通常技の紐付け--------");
		createStrMoveListFunc.apply((pma) -> pma.getQuickMoveList()).stream().forEach(log::info);
		log.info("-----②スペシャル技の紐付け------");
		createStrMoveListFunc.apply((pma) -> pma.getCinematicMoveList()).stream().forEach(log::info);
		log.info("------------技 - ポケモンと技 ここまで------------");
	}
}
