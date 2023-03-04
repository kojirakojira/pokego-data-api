package jp.brainjuice.pokego.business.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jp.brainjuice.pokego.business.constant.GenNameEnum;
import jp.brainjuice.pokego.business.dao.GoPokedexRepository;
import jp.brainjuice.pokego.business.dao.entity.GoPokedex;
import jp.brainjuice.pokego.web.form.res.elem.SimpPokemon;

@Service
public class UnimplPokemonService {

	private GoPokedexRepository goPokedexRepository;

	@Autowired
	public UnimplPokemonService(GoPokedexRepository goPokedexRepository) {
		this.goPokedexRepository = goPokedexRepository;
	}

	public List<SimpPokemon> getUnimplementedPokemonList() {

		List<SimpPokemon> simpPokemonList = new ArrayList<>();

		List<GoPokedex> goPokedexList = goPokedexRepository.findByImplFlg(false);

		goPokedexList.forEach(gp -> {
			simpPokemonList.add(new SimpPokemon(
					gp.getPokedexId(),
					gp.getName(),
					gp.getImage(),
					GenNameEnum.valueOf(gp.getGen()).getJpn(),
					gp.getRemarks()));
		});

		return simpPokemonList;
	}
}
