package jp.brainjuice.pokego.business.service.others;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jp.brainjuice.pokego.business.constant.GenNameEnum;
import jp.brainjuice.pokego.business.dao.GoPokedexRepository;
import jp.brainjuice.pokego.business.dao.entity.GoPokedex;
import jp.brainjuice.pokego.utils.BjUtils;
import jp.brainjuice.pokego.utils.LastUpdatedMap;
import jp.brainjuice.pokego.web.form.res.elem.SimpPokemon;
import jp.brainjuice.pokego.web.form.res.others.UnimplPokemonResponse;

@Service
public class UnimplPokemonService {

	private GoPokedexRepository goPokedexRepository;

	private LastUpdatedMap lastUpdatedMap;

	@Autowired
	public UnimplPokemonService(
			GoPokedexRepository goPokedexRepository,
			LastUpdatedMap lastUpdatedMap) {
		this.goPokedexRepository = goPokedexRepository;
		this.lastUpdatedMap = lastUpdatedMap;
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
							gp.getImage(),
							GenNameEnum.valueOf(gp.getGen()).getJpn(),
							gp.getRemarks()))
				.collect(Collectors.toList());
		res.setUnimplList(simpPokemonList);

		// 最終更新日
		String lastUpdated = lastUpdatedMap.get(LastUpdatedMap.Keys.unimplPokemon, BjUtils.sdfYmd);
		res.setLastUpdated(lastUpdated);

	}
}
