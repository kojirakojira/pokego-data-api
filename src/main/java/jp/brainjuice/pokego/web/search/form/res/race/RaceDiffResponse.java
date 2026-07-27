package jp.brainjuice.pokego.web.search.form.res.race;

import java.util.List;

import io.micrometer.common.util.StringUtils;
import jp.brainjuice.pokego.business.service.search.utils.dto.MultiSearchResult;
import jp.brainjuice.pokego.business.service.search.utils.dto.RaceDiffElem;
import jp.brainjuice.pokego.business.service.search.utils.dto.raceDiff.RaceDiffResult;
import jp.brainjuice.pokego.web.Response;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@NoArgsConstructor
public class RaceDiffResponse extends Response {

	// nameArrから検索した場合はnull
	private MultiSearchResult msr;
	// allUniqueがfalseの場合はnull
	private List<RaceDiffElem> raceDiffElemArr;

	private boolean searchedById;

	/** GOにおけるポケモン数（絞り込みをした場合はその数） */
	private int goTotalCount;
	/**
	 * 原作におけるポケモン数（絞り込みをした場合はその数。
	 * アーマードミュウツーのようなポケモンを含む場合、GOと総数が異なる。）
	 */
	private int oriTotalCount;

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
		this.setOriTotalCount(rdr.getOriTotalCount());
	}
}
