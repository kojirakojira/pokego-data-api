package jp.brainjuice.pokego.dao.jpa.init;

import java.text.MessageFormat;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import jp.brainjuice.pokego.business.service.search.utils.PokemonEditUtils;
import jp.brainjuice.pokego.dao.jpa.EvolutionRepository;
import jp.brainjuice.pokego.dao.jpa.GoPokedexRepository;
import jp.brainjuice.pokego.dao.jpa.entity.Evolution;
import jp.brainjuice.pokego.dao.jpa.entity.GoPokedex;
import jp.brainjuice.pokego.utils.exception.PokemonDataException;
import lombok.extern.slf4j.Slf4j;

/**
 * 整合性チェック。GoPokedexとEvolutionは1対1で紐づく。
 */
@Component("EvolutionConsistencyCheck")
@Slf4j
public class EvolutionConsistencyCheck {

	private static final String NOT_EXISTS_MSG = "pokemon.csvに定義したポケモンがpokemon-evolution.csvに定義されていません。{0}";

	/**
	 * @throws PokemonDataException
	 */
	public EvolutionConsistencyCheck(
			GoPokedexRepository goPokedexRepository,
			EvolutionRepository evolutionRepository) {
		check(goPokedexRepository, evolutionRepository);
	}

	/**
	 * @throws PokemonDataException
	 */
	private void check(
			GoPokedexRepository goPokedexRepository,
			EvolutionRepository evolutionRepository) {

		try {

			List<GoPokedex> goPokedexList = goPokedexRepository.findAll();
			List<Evolution> evolutionList = evolutionRepository.findAll();
			checkAllExists(evolutionList, goPokedexList);


		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new PokemonDataException(e);
		}
	}

	/**
	 * GoPokedexに生成されたポケモンが、すべてEvolutionに定義されていることを確認する。
	 *
	 * @param evoList
	 * @param goPokedexList
	 * @throws PokemonDataException
	 */
	private void checkAllExists(List<Evolution> evoList, List<GoPokedex> goPokedexList) {

		List<String> evoPidList = evoList.stream()
				.map(Evolution::getPokedexId)
				.collect(Collectors.toList());

		List<GoPokedex> notExistsGpList = goPokedexList.stream()
				.filter(gp -> !evoPidList.contains(gp.getPokedexId()))
				.collect(Collectors.toList());

		// GoPokedexリストに存在していて、Evolutionリストに存在していないポケモンがいるかどうか。
		if (!notExistsGpList.isEmpty()) {
			throw new PokemonDataException(
					MessageFormat.format(
							NOT_EXISTS_MSG,
							notExistsGpList.stream().map(PokemonEditUtils::appendRemarks).collect(Collectors.toList())));
		}
	}
}
