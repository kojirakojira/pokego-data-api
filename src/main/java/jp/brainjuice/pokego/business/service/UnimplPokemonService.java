package jp.brainjuice.pokego.business.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jp.brainjuice.pokego.business.dao.GoPokedexRepository;
import jp.brainjuice.pokego.business.dao.entity.GoPokedex;
import jp.brainjuice.pokego.business.service.utils.memory.GenNameMap;
import jp.brainjuice.pokego.cache.service.ViewsCacheProvider;
import jp.brainjuice.pokego.web.form.res.elem.SimpPokemon;

@Service
public class UnimplPokemonService {

	private GoPokedexRepository goPokedexRepository;

	private ViewsCacheProvider viewsCacheProvider;

	private GenNameMap genNameMap;

	@Autowired
	public UnimplPokemonService(
			GoPokedexRepository goPokedexRepository,
			ViewsCacheProvider viewsCacheProvider,
			GenNameMap genNameMap) {
		this.goPokedexRepository = goPokedexRepository;
		this.viewsCacheProvider = viewsCacheProvider;
		this.genNameMap = genNameMap;
	}

	public List<SimpPokemon> getUnimplementedPokemonList() {

		List<SimpPokemon> simpPokemonList = new ArrayList<>();

		List<GoPokedex> goPokedexList = goPokedexRepository.findByImplFlg(false);

		goPokedexList.forEach(gp -> {
			simpPokemonList.add(new SimpPokemon(
					gp.getPokedexId(),
					gp.getName(),
					gp.getImage(),
					genNameMap.get(gp.getGen()),
					gp.getRemarks()));
		});

		// 閲覧数を手動で追加。
		viewsCacheProvider.addTempList();
		return simpPokemonList;
	}
}
