package jp.brainjuice.pokego.dao.jpa;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Meta;
import org.springframework.data.jpa.repository.Query;

import jp.brainjuice.pokego.business.constant.Type.TypeEnum;
import jp.brainjuice.pokego.dao.jpa.dto.SimpMove;
import jp.brainjuice.pokego.dao.jpa.entity.FastAttack;

public interface FastAttackRepository extends JpaRepository<FastAttack, String> {

	@Meta(comment = "find by type in(fast attack)")
	List<FastAttack> findByTypeIn(List<TypeEnum> typeList);

	/**
	 * ポケモンが覚えるすべての技を取得する。<br>
	 * (=ポケモンが覚えない通常技を省いて取得する。)
	 *
	 * @return
	 */
	@Query(value = "SELECT DISTINCT fa.* "
			+ "FROM fast_attack fa "
			+ "INNER JOIN pokemon_fast_attack pfa "
			+ "ON fa.move_id = pfa.move_id", nativeQuery = true)
	@Meta(comment = "find fastAttack all can learn")
	List<FastAttack> findAllCanLearn();

	/**
	 * この{@link FastAttackRepository#findSimpMoveByNameLikeIn() メソッド}を呼び出すこと。<br>
	 * 処理順
	 * <ol>
	 * <li>引数で受け取った値をテーブルに変換し、値の前後に'%'を連結する。</li>
	 * <li>name列をひらがな→カタカナ変換し、通常技のCTEを作成する。</li>
	 * <li>name列をひらがな→カタカナ変換し、スペシャル技のCTEを作成する。</li>
	 * <li>CTEを使用し、NOT EXISTSとNOT LIKEでCTE「patterns」にすべて一致するレコードを特定する。（ド・モルガンの考え方） → 通常技、スペシャル技それぞれで実行する。</li>
	 * <li>通常技とスペシャル技をUNION ALLで連結する。</li>
	 * </ol>
	 *
	 * @return
	 */
	@Meta(comment = "find simpMove by name like in")
	@Query(value = "WITH patterns AS ( "
			+ "  SELECT '%' || hira_to_kata(word) || '%' AS kata_pattern "
			+ "    FROM unnest(:words) AS t(word)),"
			+ "fast_attack_tmp AS ( "
			+ "  SELECT fa.move_id, hira_to_kata(fa.name) AS kata_name, fa.name AS original_name "
			+ "    FROM fast_attack fa),"
			+ "charged_attack_tmp AS ( "
			+ "  SELECT ca.move_id, hira_to_kata(ca.name) AS kata_name, ca.name AS original_name "
			+ "    FROM charged_attack ca) "
			+ "SELECT fa_tmp.move_id, fa_tmp.original_name "
			+ "  FROM fast_attack_tmp fa_tmp "
			+ "  WHERE NOT EXISTS ("
			+ "    SELECT 1"
			+ "      FROM patterns pat"
			+ "      WHERE fa_tmp.kata_name NOT LIKE pat.kata_pattern) "
			+ "UNION ALL "
			+ "SELECT ca_tmp.move_id, ca_tmp.original_name "
			+ "  FROM charged_attack_tmp ca_tmp "
			+ "  WHERE NOT EXISTS ("
			+ "    SELECT 1"
			+ "      FROM patterns pat"
			+ "      WHERE ca_tmp.kata_name NOT LIKE pat.kata_pattern)", nativeQuery = true)
	@Deprecated(forRemoval = true)
	List<Object[]> findSimpMoveByNameLikeInRow(String[] words);

	/**
	 * ひらがな、カタカナを区別せずLIKE検索する。<br>
	 * 通常技、スペシャル技から取得し、引数の配列に対してはAND演算で取得する。
	 *
	 * @param words
	 * @return
	 */
	default List<SimpMove> findSimpMoveByNameLikeIn(String[] words) {
		return findSimpMoveByNameLikeInRow(words).stream()
				.map(SimpMove::new)
				.toList();
	};
}
