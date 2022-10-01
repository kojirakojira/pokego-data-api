package jp.brainjuice.pokego.web.form.res.research.cp;

import jp.brainjuice.pokego.web.form.res.Response;
import jp.brainjuice.pokego.web.form.res.elem.CpRank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper=false)
@NoArgsConstructor
public class CpRankResponse extends Response {

	private CpRank cpRank;
}
