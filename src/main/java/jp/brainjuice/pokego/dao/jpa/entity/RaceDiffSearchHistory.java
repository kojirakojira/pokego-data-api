package jp.brainjuice.pokego.dao.jpa.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * RaceDiff検索履歴
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@ToString
@Table(name = "race_diff_search_history")
public class RaceDiffSearchHistory implements Serializable {

    /** 検索された複数IDをソートしてハッシュ化した文字列 */
    @Id
    @Column(name = "search_hash", nullable = false, length = 64)
    private String searchHash;

    /** 選択されたポケモンID その1 */
    @Column(name = "pokedex_id_1", nullable = false, columnDefinition = "bpchar")
    private String pokedexId1;

    /** 選択されたポケモンID その2 */
    @Column(name = "pokedex_id_2", nullable = false, columnDefinition = "bpchar")
    private String pokedexId2;

    /** 選択されたポケモンID その3 */
    @Column(name = "pokedex_id_3", columnDefinition = "bpchar")
    private String pokedexId3;

    /** 選択されたポケモンID その4 */
    @Column(name = "pokedex_id_4", columnDefinition = "bpchar")
    private String pokedexId4;

    /** 選択されたポケモンID その5 */
    @Column(name = "pokedex_id_5", columnDefinition = "bpchar")
    private String pokedexId5;

    /** 選択されたポケモンID その6 */
    @Column(name = "pokedex_id_6", columnDefinition = "bpchar")
    private String pokedexId6;

    /** その組み合わせが検索された合計回数 */
    @Column(name = "search_count", nullable = false)
    private int searchCount;

    /** 最後に検索された日時 */
    @Column(name = "last_searched_at", nullable = false)
    private LocalDateTime lastSearchedAt;

}
