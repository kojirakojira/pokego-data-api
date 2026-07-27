package jp.brainjuice.pokego.dao.jpa;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import jp.brainjuice.pokego.dao.jpa.entity.Citations;
import jp.brainjuice.pokego.dao.jpa.entity.CitationsPk;

public interface CitationsRepository extends JpaRepository<Citations, CitationsPk> {

    List<Citations> findByPageIdOrderByDisplayOrder(String pageId);
}
