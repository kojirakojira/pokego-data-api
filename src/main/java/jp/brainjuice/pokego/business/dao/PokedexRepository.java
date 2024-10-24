package jp.brainjuice.pokego.business.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import jp.brainjuice.pokego.business.dao.entity.Pokedex;

/**
 * 原作におけるポケモンの情報を取得するRepositoryクラス
 *
 * @author saibabanagchampa
 *
 */
@Repository
public interface PokedexRepository extends JpaRepository<Pokedex, String> {
}
