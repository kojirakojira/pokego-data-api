package jp.brainjuice.pokego.web.search.form.res.race;

import java.util.List;

import io.micrometer.common.util.StringUtils;
import jp.brainjuice.pokego.business.service.search.utils.dto.RaceDiffElem;
import jp.brainjuice.pokego.business.service.search.utils.dto.raceDiff.RaceDiffResult;
import jp.brainjuice.pokego.web.search.form.res.ResearchResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class RaceDiffFrequencyResponse extends ResearchResponse {

    private List<RaceDiffElem> raceDiffElemArr;
    private int goTotalCount;

    public void setRaceDiffResult(RaceDiffResult rdr) {
        this.setSuccess(rdr.isSuccess());
        if (!StringUtils.isEmpty(rdr.getMessage())) {
            this.setMessage(rdr.getMessage());
        }
        if (rdr.getMsgLevel() != null) {
            this.setMsgLevel(rdr.getMsgLevel());
        }
        this.setRaceDiffElemArr(rdr.getRaceDiffElemArr());
        this.setGoTotalCount(rdr.getGoTotalCount());
    }
}
