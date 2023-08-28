package jp.brainjuice.pokego.web.form.res.cp;

import java.util.ArrayList;

import jp.brainjuice.pokego.business.dao.entity.GoPokedex;
import jp.brainjuice.pokego.web.form.res.ResearchResponse;
import jp.brainjuice.pokego.web.form.res.elem.CpRank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper=false)
@AllArgsConstructor
@NoArgsConstructor
public class CpRankListResponse extends ResearchResponse {

	private GoPokedex goPokedex;
	private ArrayList<CpRank> cpRankList;
}
