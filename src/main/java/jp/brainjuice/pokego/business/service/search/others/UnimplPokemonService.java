package jp.brainjuice.pokego.business.service.search.others;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import jp.brainjuice.pokego.dao.jpa.GoPokedexRepository;
import jp.brainjuice.pokego.dao.jpa.entity.GoPokedex;
import jp.brainjuice.pokego.utils.LastUpdateService;
import jp.brainjuice.pokego.utils.LastUpdateService.Keys;
import jp.brainjuice.pokego.web.search.form.res.elem.SimpPokemon;
import jp.brainjuice.pokego.web.search.form.res.others.UnimplPokemonResponse;

@Service
public class UnimplPokemonService {

	private GoPokedexRepository goPokedexRepository;

	private LastUpdateService lastUpdateService;

	public UnimplPokemonService(
			GoPokedexRepository goPokedexRepository,
			LastUpdateService lastUpdateService) {
		this.goPokedexRepository = goPokedexRepository;
		this.lastUpdateService = lastUpdateService;
	}

	/**
	 * 未実装ポケモン一覧を取得する。
	 *
	 * @return
	 */
	public void exec(UnimplPokemonResponse res) {

		List<GoPokedex> goPokedexList = goPokedexRepository.findByImplFlg(false);

		// 未実装ポケモン一覧
		List<SimpPokemon> simpPokemonList = goPokedexList.stream()
				.map(gp -> new SimpPokemon(
							gp.getPokedexId(),
							gp.getName(),
							gp.getImage1(),
							gp.getGen().getJpn(),
							gp.getRemarks()))
				.collect(Collectors.toList());
		res.setUnimplList(simpPokemonList);

		// 最終更新日
		String lastUpdated = lastUpdateService.getYmd(Keys.unimplPokemon);
		res.setLastUpdated(lastUpdated);

	}
}
