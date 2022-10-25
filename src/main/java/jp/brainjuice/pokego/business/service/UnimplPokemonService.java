package jp.brainjuice.pokego.business.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jp.brainjuice.pokego.business.dao.GoPokedexRepository;
import jp.brainjuice.pokego.business.dao.entity.GoPokedex;

@Service
public class UnimplPokemonService {

	private GoPokedexRepository goPokedexRepository;

	@Autowired
	public UnimplPokemonService(GoPokedexRepository goPokedexRepository) {
		this.goPokedexRepository = goPokedexRepository;
	}

	public List<GoPokedex> getUnimplementedPokemonList() {
		List<GoPokedex> goPokedexList = goPokedexRepository.findByImplFlg(false);
		return goPokedexList;
	}
}
