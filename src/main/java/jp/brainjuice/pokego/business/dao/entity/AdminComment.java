package jp.brainjuice.pokego.business.dao.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Data;

/**
 * サイト運営者からユーザに向けたお知らせ
 */
@Entity
@Data
@Table(name = "admin_comment")
public class AdminComment {

	/** PL(ポケモンレベル) */
	@Id
	@Column(nullable = false)
	private int commentId;

	/** 倍率 */
	@Column(nullable = false)
	private String comment;
}
