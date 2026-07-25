package jp.brainjuice.pokego.dao.jpa;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jp.brainjuice.pokego.business.service.search.utils.PokemonEditUtils;
import jp.brainjuice.pokego.dao.jpa.entity.RaceDiffSearchHistory;

public interface RaceDiffSearchHistoryRepository extends JpaRepository<RaceDiffSearchHistory, String> {

    /**
     * 最も検索された比較の組み合わせを取得する。
     * 
     * @param pid
     * @return
     */
    @Query(value = "SELECT * FROM race_diff_search_history r WHERE :pid IN (r.pokedex_id_1, r.pokedex_id_2, r.pokedex_id_3, r.pokedex_id_4, r.pokedex_id_5, r.pokedex_id_6) ORDER BY r.search_count DESC LIMIT 1", nativeQuery = true)
    RaceDiffSearchHistory findMostFrequentByPid(@Param("pid") String pid);

    /**
     * PokedexIdのリストからハッシュ（主キー）を生成し、エンティティを登録する。
     * 
     * @param entity
     * @return
     */
    default RaceDiffSearchHistory insertWithHash(RaceDiffSearchHistory entity) {
        List<String> ids = new ArrayList<>();
        if (entity.getPokedexId1() != null) ids.add(entity.getPokedexId1());
        if (entity.getPokedexId2() != null) ids.add(entity.getPokedexId2());
        if (entity.getPokedexId3() != null) ids.add(entity.getPokedexId3());
        if (entity.getPokedexId4() != null) ids.add(entity.getPokedexId4());
        if (entity.getPokedexId5() != null) ids.add(entity.getPokedexId5());
        if (entity.getPokedexId6() != null) ids.add(entity.getPokedexId6());

        // pokedex_idの並び替えロジックを適用
        ids.sort(PokemonEditUtils.getPokedexIdComparator());

        // カンマ区切りで結合し、SHA-256でハッシュ化
        String joined = String.join(",", ids);
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(joined.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            entity.setSearchHash(sb.toString());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }

        return save(entity);
    }
}
