package jp.brainjuice.pokego.dao.jpa;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Meta;
import org.springframework.data.jpa.repository.Query;

import jp.brainjuice.pokego.dao.jpa.entity.PokemonAttackPk;
import jp.brainjuice.pokego.dao.jpa.entity.PokemonChargedAttack;

public interface PokemonChargedAttackRepository extends JpaRepository<PokemonChargedAttack, PokemonAttackPk> {

	@Meta(comment = "find by id join fetch charged attack")
	@Query(value = "SELECT pca "
			+ "FROM PokemonChargedAttack pca "
			+ "JOIN FETCH pca.chargedAttack "
			+ "LEFT JOIN FETCH pca.attackAdditionalInfo "
			+ "WHERE pca.pokedexId = :pokedexId")
	List<PokemonChargedAttack> findByPokedexIdJoinChargedAttack(String pokedexId);

	@Meta(comment = "find by pokedex id")
	List<PokemonChargedAttack> findByPokedexId(String pokedexId);
}
