package jp.brainjuice.pokego.web.search.form.res.cp;

import java.util.ArrayList;

import jp.brainjuice.pokego.dao.jpa.entity.GoPokedex;
import jp.brainjuice.pokego.web.search.form.res.ResearchResponse;
import jp.brainjuice.pokego.web.search.form.res.elem.CpRank;
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
