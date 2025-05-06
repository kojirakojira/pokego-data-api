package jp.brainjuice.pokego.business.service.search.others;

import java.util.List;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;

import jp.brainjuice.pokego.dao.jpa.GoPokedexRepository;
import jp.brainjuice.pokego.dao.jpa.entity.GoPokedex;
import jp.brainjuice.pokego.utils.BjUtils;
import jp.brainjuice.pokego.utils.LastUpdatedMap;
import jp.brainjuice.pokego.web.search.form.res.others.DynamaxImplPokemonResponse;

@Service
public class DynamaxImplPokemonService {

	private GoPokedexRepository goPokedexRepository;

	private LastUpdatedMap lastUpdatedMap;

	public DynamaxImplPokemonService(
			GoPokedexRepository goPokedexRepository,
			LastUpdatedMap lastUpdatedMap) {
		this.goPokedexRepository = goPokedexRepository;
		this.lastUpdatedMap = lastUpdatedMap;
	}

	/**
	 * ダイマックス、キョダイマックス実装済みポケモンの一覧を取得する。
	 *
	 * @return
	 */
	public void exec(DynamaxImplPokemonResponse res) {

		List<GoPokedex> goPokedexList = goPokedexRepository.findByDynamaxImplFlgTrueOrGigantamaxImplFlgTrue();
		List<GoPokedex> flatedGpList = goPokedexList.stream()
				.flatMap(gp -> {
					if (gp.isDynamaxImplFlg() && gp.isGigantamaxImplFlg()) {
						// ダイマックス、キョダイマックス両方可能な場合は、2つに分離する。
						GoPokedex gpCopy = gp.clone();
						gp.setGigantamaxImplFlg(false);
						gpCopy.setDynamaxImplFlg(false);
						return Stream.of(gp, gpCopy);
					}
					return Stream.of(gp);
				})
				.toList();

		res.setDynamaxList(flatedGpList);

		// 最終更新日
		String lastUpdated = lastUpdatedMap.get(LastUpdatedMap.Keys.dynamaxImplPokemon, BjUtils.sdfYmd);
		res.setLastUpdated(lastUpdated);

	}
}
