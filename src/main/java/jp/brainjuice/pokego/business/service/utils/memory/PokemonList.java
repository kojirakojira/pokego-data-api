//package jp.brainjuice.pokego.business.service.utils.memory;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import javax.annotation.PostConstruct;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
//import jp.brainjuice.pokego.business.dao.PokedexRepository;
//import jp.brainjuice.pokego.business.dao.entity.Pokedex;
//import lombok.extern.slf4j.Slf4j;
//
//
//@Component
//@Slf4j
//public class PokemonList extends ArrayList<String> {
//
//	private PokedexRepository pokedexRepository;
//
//	@Autowired
//	public PokemonList(PokedexRepository pokedexRepository) {
//		this.pokedexRepository = pokedexRepository;
//	}
//
//	@PostConstruct
//	public void init() {
//
//		try {
//			List<Pokedex> pokedexList = pokedexRepository.findAll();
//			pokedexList.forEach(p -> add(p.getName()));
//
//			log.debug("PokemonList: " + this.toString());
//			log.info("PokemonList generated!!");
//		} catch (Exception e) {
//			log.error(e.getMessage(), e);
//		}
//	}
//}
