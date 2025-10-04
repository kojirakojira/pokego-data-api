package jp.brainjuice.pokego.business.service.manage.masterFileAnalyzer;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;

import com.ibm.icu.text.MessageFormat;

import jp.brainjuice.pokego.business.service.manage.masterFileAnalyzer.dto.PokemonMoveAll;
import jp.brainjuice.pokego.dao.jpa.ChargedAttackRepository;
import jp.brainjuice.pokego.dao.jpa.FastAttackRepository;
import jp.brainjuice.pokego.dao.jpa.PokemonChargedAttackRepository;
import jp.brainjuice.pokego.dao.jpa.PokemonFastAttackRepository;
import jp.brainjuice.pokego.dao.jpa.entity.ChargedAttack;
import jp.brainjuice.pokego.dao.jpa.entity.FastAttack;
import jp.brainjuice.pokego.dao.jpa.entity.PokemonChargedAttack;
import jp.brainjuice.pokego.dao.jpa.entity.PokemonFastAttack;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PokemonMoveCreator {

	private FastAttackRepository fastAttackRepository;

	private ChargedAttackRepository chargedAttackRepository;

	private PokemonFastAttackRepository pokemonFastAttackRepository;

	private PokemonChargedAttackRepository pokemonChargedAttackRepository;

	public PokemonMoveCreator(
			FastAttackRepository fastAttackRepository,
			ChargedAttackRepository chargedAttackRepository,
			PokemonFastAttackRepository pokemonFastAttackRepository,
			PokemonChargedAttackRepository pokemonChargedAttackRepository) {
		this.fastAttackRepository = fastAttackRepository;
		this.chargedAttackRepository = chargedAttackRepository;
		this.pokemonFastAttackRepository = pokemonFastAttackRepository;
		this.pokemonChargedAttackRepository = pokemonChargedAttackRepository;
	}

	void createAndSaveFastAttack(Collection<PokemonMoveAll> pokemonMoveList, boolean shouldSaveFastAttack) {

		log.info("------------ポケモンが覚える通常技の登録 ここから------------");

		// マスタデータから作成
		List<PokemonFastAttack> pokemonFastAttack;
		{
			List<FastAttack> fastAttackList = fastAttackRepository.findAll();
			pokemonFastAttack = createPokemonFastAttackList(fastAttackList, pokemonMoveList);
		}
		{
			// DBから取得
			List<PokemonFastAttack> pokemonFastAttackFromDbList = pokemonFastAttackRepository.findAll();
			pokemonFastAttack = filterSaveTargetPokemonFastAttack(pokemonFastAttack, pokemonFastAttackFromDbList);

		}
		if (shouldSaveFastAttack) {
			pokemonFastAttackRepository.saveAll(pokemonFastAttack);
			log.info("PokemonFastAttackをDBに登録しました。");
		} else {
			log.info("PokemonFastAttackのDB登録は実行しませんでした。");
		}
		log.info("------------ポケモンが覚える通常技の登録 ここまで------------");
	}

	private List<PokemonFastAttack> createPokemonFastAttackList(List<FastAttack> fastAttackList, Collection<PokemonMoveAll> pokemonMoveList) {

		Map<String, String> uniqueIdMoveIdMap = fastAttackList.stream()
				.collect(Collectors.toMap(fa -> fa.getUniqueId(), fa -> fa.getMoveId()));

		List<PokemonFastAttack> pokemonFastAttackList = pokemonMoveList.stream()
				.flatMap(pma -> {
					return pma.getQuickMoveList().stream()
							.map(pm -> new PokemonFastAttack(
									uniqueIdMoveIdMap.get(pm.getMovementId()),
									pma.getPokedexId(),
									pm.getCategory(),
									null,
									null,
									null));
				})
				.toList();

		return pokemonFastAttackList;
	}

	private List<PokemonFastAttack> filterSaveTargetPokemonFastAttack(List<PokemonFastAttack> pfaFromMdList, List<PokemonFastAttack> pfaFromDbList) {

		log.info(MessageFormat.format("ポケモン通常技マスタデータ件数: {0}件, DB件数: {1}件", pfaFromMdList.size(), pfaFromDbList.size()));
		log.info("------------【ポケモン通常技】マスタデータにあるが、DBにないやつ ここから------------");
		List<PokemonFastAttack> mdYesDbNoList = pfaFromMdList.stream()
				.filter(pfa -> !pfaFromDbList.contains(pfa))
				.toList();
		mdYesDbNoList.stream().forEach(pfa -> log.info(pfa.toString()));
		log.info("------------【ポケモン通常技】マスタデータにあるが、DBにないやつ ここまで------------");
		log.info("------------【ポケモン通常技】DBにあるが、マスタデータにないやつ ここから------------");
		List<PokemonFastAttack> mdNoDbYesList = pfaFromDbList.stream()
				.filter(pfa -> !pfaFromMdList.contains(pfa))
				.toList();
		// FastAttackテーブルから情報を取得して作成しているため、有り得ないパターンのはず
		mdNoDbYesList.stream().forEach(pfa -> log.warn(pfa.toString()));
		log.info("------------【ポケモン通常技】DBにあるが、マスタデータにないやつ ここまで------------");
		log.info("------------【ポケモン通常技】DBとマスタデータで値が異なる ここから------------");
		List<PokemonFastAttack> differentList = pfaFromMdList.stream()
				.filter(pfa -> !mdYesDbNoList.contains(pfa))
				.filter(pfa -> {
					PokemonFastAttack pfaFromDb = null;
					// マスタデータとpokedexId,moveIdが一致するPokemonFastAttackを抜き出す。（hashCode,equals実装済み）
					for (PokemonFastAttack tmpPfaFromDb: pfaFromDbList) {
						if (pfa.equals(tmpPfaFromDb)) {
							pfaFromDb = tmpPfaFromDb;
							break;
						}
					}
					// 覚え方も一致するやつを排除
					return pfa.getLearningPattern() != pfaFromDb.getLearningPattern();
				})
				.toList();
		differentList.stream().forEach(pfa -> log.info(pfa.toString()));
		log.info("------------【ポケモン通常技】DBとマスタデータで値が異なる ここまで------------");
		log.info("------------【ポケモン通常技】マスタデータ重複チェック ここから------------");
		{
			Map<PokemonFastAttack, Long> distinctCountMap = pfaFromMdList.stream()
					.collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
			List<PokemonFastAttack> distinctList = distinctCountMap.entrySet().stream()
					.filter(entry -> entry.getValue().longValue() > 1L)
					.map(Map.Entry::getKey)
					.toList();
			distinctList.stream().forEach(pfa -> log.warn(pfa.toString()));
		}
		log.info("------------【ポケモン通常技】マスタデータ重複チェック ここまで------------");

		return Stream.concat(
				mdYesDbNoList.stream(),
				differentList.stream())
				.toList();
	}

	void createAndSaveChargedAttack(Collection<PokemonMoveAll> pokemonMoveList, boolean shouldSaveChargedAttack) {

		log.info("------------ポケモンが覚えるスペシャル技の登録 ここから------------");

		// マスタデータから作成
		List<PokemonChargedAttack> pokemonChargedAttack;
		{
			List<ChargedAttack> chargedAttackList = chargedAttackRepository.findAll();
			pokemonChargedAttack = createPokemonChargedAttackList(chargedAttackList, pokemonMoveList);
		}
		{
			// DBから取得
			List<PokemonChargedAttack> pokemonChargedAttackFromDbList = pokemonChargedAttackRepository.findAll();
			pokemonChargedAttack = filterSaveTargetPokemonChargedAttack(pokemonChargedAttack, pokemonChargedAttackFromDbList);

		}
		if (shouldSaveChargedAttack) {
			pokemonChargedAttackRepository.saveAll(pokemonChargedAttack);
			log.info("PokemonChargedAttackをDBに登録しました。");
		} else {
			log.info("PokemonChargedAttackのDB登録は実行しませんでした。");
		}
		log.info("------------ポケモンが覚えるスペシャル技の登録 ここまで------------");
	}

	private List<PokemonChargedAttack> createPokemonChargedAttackList(List<ChargedAttack> chargedAttackList, Collection<PokemonMoveAll> pokemonMoveList) {

		Map<String, String> uniqueIdMoveIdMap = chargedAttackList.stream()
				.collect(Collectors.toMap(ca -> ca.getUniqueId(), ca -> ca.getMoveId()));

		List<PokemonChargedAttack> pokemonChargedAttackList = pokemonMoveList.stream()
				.flatMap(pma -> {
					return pma.getCinematicMoveList().stream()
							.map(pm -> new PokemonChargedAttack(
									uniqueIdMoveIdMap.get(pm.getMovementId()),
									pma.getPokedexId(),
									pm.getCategory(),
									null,
									null,
									null));
				})
				.toList();

		return pokemonChargedAttackList;
	}

	private List<PokemonChargedAttack> filterSaveTargetPokemonChargedAttack(
			List<PokemonChargedAttack> pcaFromMdList,
			List<PokemonChargedAttack> pcaFromDbList) {

		log.info(MessageFormat.format("ポケモンスペシャル技マスタデータ件数: {0}件, DB件数: {1}件", pcaFromMdList.size(), pcaFromDbList.size()));
		log.info("------------【ポケモンスペシャル技】マスタデータにあるが、DBにないやつ ここから------------");
		List<PokemonChargedAttack> mdYesDbNoList = pcaFromMdList.stream()
				.filter(pca -> !pcaFromDbList.contains(pca))
				.toList();
		mdYesDbNoList.stream().forEach(pca -> log.info(pca.toString()));
		log.info("------------【ポケモンスペシャル技】マスタデータにあるが、DBにないやつ ここまで------------");
		log.info("------------【ポケモンスペシャル技】DBにあるが、マスタデータにないやつ ここから------------");
		List<PokemonChargedAttack> mdNoDbYesList = pcaFromDbList.stream()
				.filter(pca -> !pcaFromMdList.contains(pca))
				.toList();
		// ChargedAttackテーブルから情報を取得して作成しているため、有り得ないパターンのはず
		mdNoDbYesList.stream().forEach(pca -> log.warn(pca.toString()));
		log.info("------------【ポケモンスペシャル技】DBにあるが、マスタデータにないやつ ここまで------------");
		log.info("------------【ポケモンスペシャル技】DBとマスタデータで値が異なる ここから------------");
		List<PokemonChargedAttack> differentList;
		{
			differentList = pcaFromMdList.stream()
			.filter(pca -> !mdYesDbNoList.contains(pca))
			.filter(pca -> {
				PokemonChargedAttack pcaFromDb = null;
				// マスタデータとpokedexId,moveIdが一致するPokemonChargedAttackを抜き出す。（hashCode,equals実装済み）
				for (PokemonChargedAttack tmpPfaFromDb: pcaFromDbList) {
					if (pca.equals(tmpPfaFromDb)) {
						pcaFromDb = tmpPfaFromDb;
						break;
					}
				}
				// 覚え方も一致するやつを排除
				return pca.getLearningPattern() != pcaFromDb.getLearningPattern();
			})
			.toList();
			// ログ出力
			differentList.stream().forEach(pca -> {
				// マスタデータとpokedexId,moveIdが一致するPokemonChargedAttackを抜き出す。（hashCode,equals実装済み）
				for (PokemonChargedAttack tmpPfaFromDb: pcaFromDbList) {
					if (pca.equals(tmpPfaFromDb)) {
						log.info(MessageFormat.format("マスタデータ:{0}, DB:{1}", pca.toString(), tmpPfaFromDb.toString()));
						break;
					}
				}
			});
		}
		log.info("------------【ポケモンスペシャル技】DBとマスタデータで値が異なる ここまで------------");
		log.info("------------【ポケモンスペシャル技】マスタデータ重複チェック ここから------------");
		{
			Map<PokemonChargedAttack, Long> distinctCountMap = pcaFromMdList.stream()
					.collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
			List<PokemonChargedAttack> distinctList = distinctCountMap.entrySet().stream()
					.filter(entry -> entry.getValue().longValue() > 1L)
					.map(Map.Entry::getKey)
					.toList();
			distinctList.stream().forEach(pca -> log.warn(pca.toString()));
		}
		log.info("------------【ポケモンスペシャル技】マスタデータ重複チェック ここまで------------");

		return Stream.concat(
				mdYesDbNoList.stream(),
				differentList.stream())
				.toList();
	}
}
