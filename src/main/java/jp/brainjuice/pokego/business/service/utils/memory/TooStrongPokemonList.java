package jp.brainjuice.pokego.business.service.utils.memory;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jp.brainjuice.pokego.business.dao.PokedexRepository;
import jp.brainjuice.pokego.business.dao.entity.Pokedex;
import jp.brainjuice.pokego.business.service.utils.PokemonEditUtils;
import jp.brainjuice.pokego.business.service.utils.PokemonGoUtils;
import jp.brainjuice.pokego.business.service.utils.PokemonUtils;
import jp.brainjuice.pokego.utils.exception.PokemonDataInitException;
import lombok.extern.slf4j.Slf4j;

/**
 * 強ポケ補正対象のポケモンを保持する。
 *
 * @author saibabanagchampa
 *
 */
@Component
@Slf4j
public class TooStrongPokemonList extends ArrayList<String> {

	@Autowired
	public TooStrongPokemonList (
			PokedexRepository pokedexRepository,
			PokemonGoUtils pokemonGoUtils,
			PokemonEditUtils pokemonEditUtils) throws PokemonDataInitException {
		init(pokedexRepository, pokemonGoUtils, pokemonEditUtils);
	}

	public void init(
			PokedexRepository pokedexRepository,
			PokemonGoUtils pokemonGoUtils,
			PokemonEditUtils pokemonEditUtils) throws PokemonDataInitException {

		// 依存関係の都合でDI管理外で生成。
		PokemonUtils pokemonUtils = new PokemonUtils(pokemonGoUtils);

		List<Pokedex> pokeList = pokedexRepository.findAll();
		pokeList.forEach(p -> {
			// メガシンカを除く。
			if (!pokemonEditUtils.isMega(p.getPokedexId())) {
				int cp = pokemonUtils.calcBaseCpFromMain(p);

				if (cp > 4000) {
					// 原作→GOの単純変換でCPが4000を超えるポケモン
					add(p.getPokedexId());

					log.debug(MessageFormat.format("{0}({1}):CP{2}", p.getName(), p.getRemarks(), cp));
				}
			}
		});

		outputLog(pokeList);

	}

	/**
	 * 補正対象のポケモンをリスト化し、ログ出力します。
	 *
	 * @param pokeList
	 */
	private void outputLog(List<Pokedex> pokeList) {

		List<String> outputList = new ArrayList<String>();
		this.forEach(pid -> {
			pokeList.forEach(p -> {
				if (pid.equals(p.getPokedexId())) {
					String remarks = StringUtils.isEmpty(p.getRemarks()) ? "" : MessageFormat.format("({0})", p.getRemarks());
					outputList.add(MessageFormat.format("{0}:{1}{2}", p.getPokedexId(), p.getName(), remarks));
				}
			});
		});

		log.info("Identifier pokemon list: " + outputList.toString());
		log.info("IdentifierPokemonList generated!!");

	}
}
