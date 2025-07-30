package jp.brainjuice.pokego.dao.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import jp.brainjuice.pokego.dao.jpa.entity.PokemonAttackPk;
import jp.brainjuice.pokego.dao.jpa.entity.PokemonChargedAttack;

public interface PokemonChargedAttackRepository extends JpaRepository<PokemonChargedAttack, PokemonAttackPk> {
}
