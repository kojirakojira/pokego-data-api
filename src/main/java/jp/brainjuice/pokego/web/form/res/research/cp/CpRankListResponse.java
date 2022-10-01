package jp.brainjuice.pokego.web.form.res.research.cp;

import java.util.ArrayList;

import jp.brainjuice.pokego.web.form.res.Response;
import jp.brainjuice.pokego.web.form.res.elem.CpRank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper=false)
@AllArgsConstructor
@NoArgsConstructor
public class CpRankListResponse extends Response {

	private ArrayList<CpRank> cpRankList;
}
