package jp.brainjuice.pokego.business.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import jp.brainjuice.pokego.business.dao.entity.PageViews;

@Repository
public interface PageViewsRepository extends JpaRepository<PageViews, String> {

}
