package jp.brainjuice.pokego.business.service.manage.masterFileAnalyzer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.icu.text.MessageFormat;

import jp.brainjuice.pokego.business.service.manage.masterFileAnalyzer.dto.AdditionalMove;
import jp.brainjuice.pokego.business.service.manage.masterFileAnalyzer.dto.CinematicMoveAll;
import jp.brainjuice.pokego.business.service.manage.masterFileAnalyzer.dto.ParsedMasterData;
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

	private enum MasterLinkDataKey {
		pokemon,
		move,
		limited_time_learned_cinematic_moves,
		not_defined_cinematic_moves
	}

	private ObjectMapper objectMapper;

	private GoPokedexRepository goPokedexRepository;

	private static final String MASTER_LINK_DATA_FILE = "pokemon/master_link_data.yml";

	public MasterFileAnalyzerService(ObjectMapper objectMapper, GoPokedexRepository goPokedexRepository) {
		this.objectMapper = objectMapper;
		this.goPokedexRepository = goPokedexRepository;
	}

	public List<String> analyze(MultipartFile masterFile) throws StreamReadException, DatabindException, IOException {

		ParsedMasterData parsedMasterData = loadMasterData(masterFile);

		Map<String, Object> masterLinkMap = loadMasterLinkData();

		Map<String, GoPokedex> gpMap = goPokedexRepository.findAll().stream()
				.collect(Collectors.toMap(
						gp -> gp.getPokedexId(),
						gp -> gp));

		// ペリずかん上のポケモンのデータが正しいかチェックする。
		checkPeriData(parsedMasterData.getPokemonDataList(), gpMap, masterLinkMap);

		log.info("------------技 - 通常技一覧一覧ここから------------");
		{
			// ポケモンが覚えてる技
			List<String> usedQuickMoveList = parsedMasterData.getPokemonDataList().stream()
					.flatMap(pd -> {
						List<String> moves = new ArrayList<>();
						if (pd.getPokemonSettings().getQuickMoves() != null) {
							moves.addAll(pd.getPokemonSettings().getQuickMoves());
						}
						if (pd.getPokemonSettings().getEliteQuickMove() != null) {
							moves.addAll(pd.getPokemonSettings().getEliteQuickMove());
						}
						return moves.stream();
					})
					.sorted()
					.distinct()
					.toList();

			// どのポケモンも覚えていない技
			List<String> unusedQuickMoveList = parsedMasterData.getQuickMoveList().stream()
					.filter(qma -> !usedQuickMoveList.contains(qma.getGymRaid().getMovementId()))
					.map(qma -> qma.getMovementId())
					.toList();

			if (unusedQuickMoveList.isEmpty()) {
				log.info("誰も覚えていない技は1件もありませんでした。");
			} else {
				unusedQuickMoveList.stream().forEach(mid -> {
					log.info(MessageFormat.format("誰も覚えていない技：movementId:\t{0}", mid));
				});
			}

			parsedMasterData.getQuickMoveList().stream()
			.filter(qma -> !unusedQuickMoveList.contains(qma.getGymRaid().getMovementId())) // 誰も覚えていない技は省く
			.map(qma -> List.of(
					qma.getMovementId(),
					qma.getGymRaid().getPokemonType(),
					String.valueOf(qma.getGymRaid().getPower()),
					String.valueOf(qma.getPvp().getPower())
					))
			.map(list -> String.join("\t", list))
			.forEach(log::info);
		}
		log.info("------------技 - 通常技一覧一覧ここまで------------");

		log.info("------------技 - スペシャル技一覧一覧ここから------------");
		{
			// ポケモンが覚えてる技
			List<String> canUseCinematicMoveList = parsedMasterData.getPokemonDataList().stream()
					.flatMap(pd -> {
						List<String> moves = new ArrayList<>();
						if (pd.getPokemonSettings().getCinematicMoves() != null) {
							moves.addAll(pd.getPokemonSettings().getCinematicMoves());
						}
						if (pd.getPokemonSettings().getEliteCinematicMove() != null) {
							moves.addAll(pd.getPokemonSettings().getEliteCinematicMove());
						}
						// ガリョウテンセイ
						if (pd.getPokemonSettings().getNonTmCinematicMoves() != null) {
							moves.addAll(pd.getPokemonSettings().getNonTmCinematicMoves());
						}
						return moves.stream();
					})
					.sorted()
					.distinct()
					.toList();
			// シャドウ、リトレーン、フォルムチェンジで覚える技
			List<String> additionalMoveList = parsedMasterData.getAdditionalCinematicMoveMap().entrySet().stream()
					.flatMap(entry -> entry.getValue().stream())
					.map(AdditionalMove::getMovementId)
					.distinct()
					.toList();

			// どのポケモンも覚えていない技
			List<String> cantCinematicMoveList = parsedMasterData.getCinematicMoveList().stream()
					.filter(qma -> {
						String mid = qma.getGymRaid().getMovementId();
						return !canUseCinematicMoveList.contains(mid) && !additionalMoveList.contains(mid);
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


			parsedMasterData.getCinematicMoveList().stream()
			.filter(qma -> !cantCinematicMoveList.contains(qma.getGymRaid().getMovementId())) // 誰も覚えていない技は省く
			.map(cma -> List.of(
					cma.getMovementId(),
					cma.getGymRaid().getPokemonType(),
					String.valueOf(cma.getGymRaid().getPower()),
					String.valueOf(cma.getPvp().getPower())
					))
			.map(list -> String.join("\t", list))
			.forEach(log::info);
		}
		log.info("------------技 - スペシャル技一覧一覧ここまで------------");

		// ポケモンごとの技を出力する。
		printMovesForEachPokemon(parsedMasterData.getPokemonDataList(), gpMap, masterLinkMap);

		return null;
	}

	private ParsedMasterData loadMasterData(MultipartFile masterFile) throws StreamReadException, DatabindException, IOException {

		List<QuickMoveAll> quickMoveList;
		List<CinematicMoveAll> cinematicMoveList;
		List<PokemonData> pokemonDataList;
		// PokemonDataのcinematicMovesに含まれない技。Map<templateId(ポケモン), List<<スペシャル技>>
		Map<String, List<AdditionalMove>> additionalCinematicMoveMap;
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
								return new QuickMoveAll(
										movementId,
										movementNo,
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
								return new CinematicMoveAll(
										movementId,
										movementNo,
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
						AdditionalMove ad = new AdditionalMove(shadow.getShadowChargeMove(), MoveCategory.shadow);
						additionalCinematicMoveMap.computeIfAbsent(templateId, k -> new ArrayList<>()).add(ad);
					}

					if (shadow.getPurifiedChargeMove() != null) {
						AdditionalMove ad = new AdditionalMove(shadow.getPurifiedChargeMove(), MoveCategory.purified);
						additionalCinematicMoveMap.computeIfAbsent(templateId, k -> new ArrayList<>()).add(ad);
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
						List<Map.Entry<String, String>> pokemonMoveList = fc.getAvailableForm().stream()
								.flatMap(aForm -> {
									// form -> Map.Entry<form, movementId>に変換
									return movementIdList.stream()
											.map(mid -> Map.entry(aForm, mid));
								})
								.map(entry -> {
									// Map.Entry<form, movementId> -> Map.Entry<templateId, movementId> の変換
									String templateId = pokemonDataList.stream()
											.filter(pd2 -> entry.getKey().equals(pd2.getPokemonSettings().getForm()))
											.findFirst()
											.orElseThrow()
											.getTemplateId();
									return Map.entry(templateId, entry.getValue());
								})
								.toList();

						pokemonMoveList.stream()
						.forEach(entry -> {
							AdditionalMove ad = new AdditionalMove(entry.getValue(), MoveCategory.formChange);
							additionalCinematicMoveMap.computeIfAbsent(entry.getValue(), k -> new ArrayList<>()).add(ad);
						});
					}
				}

				// 重複を除去する
				additionalCinematicMoveMap.entrySet().stream()
				.forEach(entry -> {
					List<AdditionalMove> ad = entry.getValue()
							.stream()
							.distinct()
							.toList();
					entry.setValue(ad);
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
		Map<String, String> masterLinkPokemonMap = ((Map<String, String>) masterLinkMap.get(MasterLinkDataKey.pokemon.name())).entrySet().stream()
		.filter(entry -> !StringUtils.isEmpty(entry.getValue()))
		.collect(Collectors.toMap(
				Map.Entry::getKey,
				Map.Entry::getValue));

		log.info("------------ポケモン - テンプレートID一覧ここから------------");
		pokemonDataList.forEach(pd -> log.info(pd.getTemplateId()));
		log.info("------------ポケモン - テンプレートID一覧ここまで------------");

		log.info("------------ポケモン - ステータスチェックここから------------");
		log.info("①マスタデータにあって、master_link_data.ymlにないやつ ここから");
		pokemonDataList.stream()
		.filter(pd -> !masterLinkPokemonMap.containsKey(pd.getTemplateId()))
		.map(pd -> pd.getTemplateId())
		.forEach(log::warn);
		log.info("①マスタデータにあって、master_link_data.ymlにないやつ ここまで");

		log.info("②ステータス検査対象外 ここから");
		log.info("(go_pokedexにあって、master_link_data.ymlにないやつ。メガシンカ、ゲンシカイキを除く)");
		{
			Set<String> pokedexIdSet = gpMap.keySet();
			Set<String> masterLinkPidSet = masterLinkPokemonMap.entrySet().stream()
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
			if (!masterLinkPokemonMap.containsKey(pd.getTemplateId())) {
				continue;
			}

			PokemonStats ps = pd.getPokemonSettings().getStats();
			GoPokedex gp = gpMap.get(masterLinkPokemonMap.get(pd.getTemplateId()));

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

	private void printMovesForEachPokemon(List<PokemonData> pokemonDataList, Map<String, GoPokedex> gpMap, Map<String, Object> masterLinkMap) {

		log.info("------------技 - ポケモンと技 ここから------------");
		@SuppressWarnings("unchecked")
		Map<String, String> masterLinkPokemonMap = (Map<String, String>) masterLinkMap.get(MasterLinkDataKey.pokemon.name());
		List<String> quickMoveStrList = new ArrayList<>();
		List<String> eliteQuickMoveStrList = new ArrayList<>();
		List<String> cinematicMoveStrList = new ArrayList<>();
		List<String> eliteCinematicMoveStrList = new ArrayList<>();
		for (PokemonData pd: pokemonDataList) {
			if (masterLinkPokemonMap.get(pd.getTemplateId()) == null) {
				continue;
			}

			if (pd.getPokemonSettings() == null) {
				// pokemonSettingsが存在しない場合スキップ(多分そんなパターンはない)
				log.warn(pd.getTemplateId() + " pokemonSettings is null");
				continue;
			}

			GoPokedex gp = gpMap.get(masterLinkPokemonMap.get(pd.getTemplateId()));
			String name = PokemonEditUtils.appendRemarks(gp);

			// 通常技
			if (pd.getPokemonSettings().getQuickMoves() == null) {
				log.warn(pd.getTemplateId() + " quickMoves is null");
			} else {
				List<String> strList = pd.getPokemonSettings().getQuickMoves().stream()
						.map(move -> MessageFormat.format("{0}\t{1}\t{2}", gp.getPokedexId(), name, move))
						.toList();
				quickMoveStrList.addAll(strList);
			}

			if (pd.getPokemonSettings().getEliteQuickMove() != null) {
				List<String> strList = pd.getPokemonSettings().getEliteQuickMove().stream()
						.map(move -> MessageFormat.format("{0}\t{1}\t{2}", gp.getPokedexId(), name, move))
						.toList();
				eliteQuickMoveStrList.addAll(strList);
			}

			// スペシャル技
			if (pd.getPokemonSettings().getCinematicMoves() == null) {
				log.warn(pd.getTemplateId() + " cinematicMoves is null");
			} else {
				List<String> strList = pd.getPokemonSettings().getCinematicMoves().stream()
						.map(move -> MessageFormat.format("{0}\t{1}\t{2}", gp.getPokedexId(), name, move))
						.toList();
				cinematicMoveStrList.addAll(strList);
			}

			if (pd.getPokemonSettings().getEliteCinematicMove() != null) {
				List<String> strList = pd.getPokemonSettings().getEliteCinematicMove().stream()
						.map(move -> MessageFormat.format("{0}\t{1}\t{2}", gp.getPokedexId(), name, move))
						.toList();
				eliteCinematicMoveStrList.addAll(strList);
			}

			if (pd.getPokemonSettings().getNonTmCinematicMoves() != null) {
				List<String> strList = pd.getPokemonSettings().getNonTmCinematicMoves().stream()
						.map(move -> MessageFormat.format("{0}\t{1}\t{2}", gp.getPokedexId(), name, move))
						.toList();
				eliteCinematicMoveStrList.addAll(strList);

			}
		}
		log.info("-----①通常技の紐付け--------");
		quickMoveStrList.stream().forEach(log::info);
		eliteQuickMoveStrList.stream().forEach(log::info);
		log.info("-----②スペシャル技の紐付け------");
		cinematicMoveStrList.stream().forEach(log::info);
		eliteCinematicMoveStrList.stream().forEach(log::info);
		log.info("------------技 - ポケモンと技 ここまで------------");
	}
}
