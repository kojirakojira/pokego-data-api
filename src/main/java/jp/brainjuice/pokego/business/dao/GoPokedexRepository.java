package jp.brainjuice.pokego.business.dao;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import jp.brainjuice.pokego.business.dao.entity.GoPokedex;

@Repository
public interface GoPokedexRepository extends JpaRepository<GoPokedex, String>, JpaSpecificationExecutor<GoPokedex> {

	List<GoPokedex> findByName(String name);

	List<GoPokedex> findByNameIn(Collection<String> name);
}
