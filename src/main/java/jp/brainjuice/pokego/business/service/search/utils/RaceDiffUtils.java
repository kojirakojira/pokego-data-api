package jp.brainjuice.pokego.business.service.search.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Component;

import jp.brainjuice.pokego.business.service.search.utils.dto.RaceDiffElem;
import jp.brainjuice.pokego.business.service.search.utils.dto.raceDiff.RaceDiffResult;
import jp.brainjuice.pokego.cache.inmemory.PokemonStatisticsInfo;
import jp.brainjuice.pokego.dao.jpa.PokedexRepository;
import jp.brainjuice.pokego.dao.jpa.entity.GoPokedex;
import jp.brainjuice.pokego.dao.jpa.entity.Pokedex;
import jp.brainjuice.pokego.web.search.form.res.MsgLevelEnum;
import jp.brainjuice.pokego.web.search.form.res.elem.Race;

@Component
public class RaceDiffUtils {

    private PokemonUtils pokemonUtils;
    private PokemonGoUtils pokemonGoUtils;
    private PokemonStatisticsInfo pokemonStatisticsInfo;
    private PokedexRepository pokedexRepository;

    private static final String MSG_NO_RESULTS = "存在しないIDが指定されました。";

    public RaceDiffUtils(
            PokedexRepository pokedexRepository,
            PokemonStatisticsInfo pokemonStatisticsInfo,
            PokemonUtils pokemonUtils,
            PokemonGoUtils pokemonGoUtils) {
        this.pokedexRepository = pokedexRepository;
        this.pokemonStatisticsInfo = pokemonStatisticsInfo;
        this.pokemonUtils = pokemonUtils;
        this.pokemonGoUtils = pokemonGoUtils;
    }

    /**
     * 種族値の比較結果を取得する。
     *
     * @param goPokedexList
     * @param idList
     * @return 種族値比較結果
     */
    public RaceDiffResult createRaceDiffResult(List<GoPokedex> goPokedexList, List<String> idList,
            boolean searchPokedexFlg) {

        RaceDiffResult result = new RaceDiffResult();
        final List<Pokedex> pokedexList = new ArrayList<>();
        if (searchPokedexFlg) {
            pokedexList.addAll((List<Pokedex>) pokedexRepository.findAllById(Objects.requireNonNull(idList)));

            if (idList.size() > pokedexList.size()) {
                result.setMsgLevel(MsgLevelEnum.error);
                result.setMessage(MSG_NO_RESULTS);
                result.setSuccess(false);
                return result;
            }
        }

        List<RaceDiffElem> raceDiffElemList = idList.stream() // 検索した時のid順で作成
                .map(pid -> {

                    Pokedex p = null;
                    if (searchPokedexFlg) {
                        p = pokedexList.stream()
                                .filter(pdx -> pdx.getPokedexId().equals(pid))
                                .findFirst().get();
                        // 原作種族値が存在しない場合は、Pokedexをnullにする。
                        p = pokemonUtils.existsOrigin(p.getPokedexId()) ? p : null;
                    }

                    GoPokedex gp = goPokedexList.stream()
                            .filter(gPdx -> gPdx.getPokedexId().equals(pid))
                            .findFirst().get();
                    return new Race(p, gp, pokemonStatisticsInfo);
                })
                .map(race -> {
                    GoPokedex gp = race.getGoPokedex();
                    int cp = pokemonGoUtils.calcBaseCp(gp.getAttack(), gp.getDefense(), gp.getHp());
                    return new RaceDiffElem(race, cp);
                })
                .toList();
        result.setRaceDiffElemArr(raceDiffElemList);

        result.setGoTotalCount(pokemonStatisticsInfo.getGoPokedexStats().getGoHpStats().getList().size());
        if (searchPokedexFlg) {
            result.setOriTotalCount(pokemonStatisticsInfo.getPokedexStats().getHpStats().getList().size());
        }

        result.setSuccess(true);
        return result;
    }
}
