package jp.brainjuice.pokego.dao.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import jp.brainjuice.pokego.dao.jpa.entity.PokemonAttackPk;
import jp.brainjuice.pokego.dao.jpa.entity.PokemonFastAttack;

public interface PokemonFastAttackRepository extends JpaRepository<PokemonFastAttack, PokemonAttackPk> {
}
