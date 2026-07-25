package jp.brainjuice.pokego.business.service.search.utils.dto.raceDiff;

import java.util.List;

import jp.brainjuice.pokego.business.service.search.utils.dto.RaceDiffElem;
import jp.brainjuice.pokego.web.Response;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class RaceDiffResult extends Response {

    // allUniqueがfalseの場合はnull
    private List<RaceDiffElem> raceDiffElemArr;

    /** GOにおけるポケモン数（絞り込みをした場合はその数） */
    private int goTotalCount;
    /**
     * 原作におけるポケモン数（絞り込みをした場合はその数。
     * アーマードミュウツーのようなポケモンを含む場合、GOと総数が異なる。）
     */
    private int oriTotalCount;
}
