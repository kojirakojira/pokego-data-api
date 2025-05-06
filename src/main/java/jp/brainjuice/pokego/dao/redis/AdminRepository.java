package jp.brainjuice.pokego.dao.redis;

import org.springframework.data.repository.CrudRepository;

import jp.brainjuice.pokego.dao.redis.entity.Admin;
import jp.brainjuice.pokego.dao.redis.entity.PageTempView;

/**
 *
 * 管理者権限を持つユーザを保持する
 *
 * @author saibabanagchampa
 * @see PageTempView
 *
 */
public interface AdminRepository extends CrudRepository<Admin, String> {

}

