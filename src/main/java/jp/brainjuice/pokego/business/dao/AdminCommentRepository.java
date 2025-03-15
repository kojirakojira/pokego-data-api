package jp.brainjuice.pokego.business.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import jp.brainjuice.pokego.business.dao.entity.AdminComment;

public interface AdminCommentRepository extends JpaRepository<AdminComment, Integer> {

}
