package jp.brainjuice.pokego.dao.jpa;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Meta;
import org.springframework.data.jpa.repository.Query;

import jp.brainjuice.pokego.dao.jpa.entity.PokemonAttackPk;
import jp.brainjuice.pokego.dao.jpa.entity.PokemonChargedAttack;

public interface PokemonChargedAttackRepository extends JpaRepository<PokemonChargedAttack, PokemonAttackPk> {

	@Meta(comment = "find all join fetch charged_attack")
	@Query(value = "SELECT pca "
			+ "FROM PokemonChargedAttack pca "
			+ "JOIN FETCH pca.chargedAttack "
			+ "LEFT JOIN FETCH pca.attackAdditionalInfo")
	List<PokemonChargedAttack> findAllJoinChargedAttack();

	@Meta(comment = "find by move_id join fetch charged_attack")
	@Query(value = "SELECT pca "
			+ "FROM PokemonChargedAttack pca "
			+ "JOIN FETCH pca.chargedAttack "
			+ "LEFT JOIN FETCH pca.attackAdditionalInfo "
			+ "WHERE pca.pokedexId = :pokedexId")
	List<PokemonChargedAttack> findByPokedexIdJoinChargedAttack(String pokedexId);

	@Meta(comment = "find by move_id join fetch go_pokedex")
	@Query(value ="SELECT pca "
			+ "FROM PokemonChargedAttack pca "
			+ "JOIN FETCH pca.goPokedex "
			+ "LEFT JOIN FETCH pca.attackAdditionalInfo "
			+ "WHERE pca.moveId = :moveId")
	List<PokemonChargedAttack> findByMoveIdJoinGoPokedex(String moveId);

	@Meta(comment = "find by pokedex id")
	List<PokemonChargedAttack> findByPokedexId(String pokedexId);
}
