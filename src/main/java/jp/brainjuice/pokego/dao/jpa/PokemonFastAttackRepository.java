package jp.brainjuice.pokego.dao.jpa;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Meta;
import org.springframework.data.jpa.repository.Query;

import jp.brainjuice.pokego.dao.jpa.entity.PokemonAttackPk;
import jp.brainjuice.pokego.dao.jpa.entity.PokemonFastAttack;

public interface PokemonFastAttackRepository extends JpaRepository<PokemonFastAttack, PokemonAttackPk> {

	@Meta(comment = "find by id join fetch fast attack")
	@Query(value = "SELECT pfa "
			+ "FROM PokemonFastAttack pfa "
			+ "JOIN FETCH pfa.fastAttack "
			+ "LEFT JOIN FETCH pfa.attackAdditionalInfo "
			+ "WHERE pfa.pokedexId = :pokedexId")
	List<PokemonFastAttack> findByPokedexIdJoinFastAttack(String pokedexId);

	@Meta(comment = "find by pokedex id")
	List<PokemonFastAttack> findByPokedexId(String pokedexId);
}
