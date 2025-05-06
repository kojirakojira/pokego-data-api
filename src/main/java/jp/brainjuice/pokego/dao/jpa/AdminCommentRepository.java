package jp.brainjuice.pokego.dao.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import jp.brainjuice.pokego.dao.jpa.entity.AdminComment;

public interface AdminCommentRepository extends JpaRepository<AdminComment, Integer> {

}
