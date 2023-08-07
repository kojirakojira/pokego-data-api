package jp.brainjuice.pokego.web.form.res.research.cp;

import java.util.List;

import jp.brainjuice.pokego.web.form.res.elem.VersatilityIv;
import jp.brainjuice.pokego.web.form.res.research.ResearchResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper=false)
@NoArgsConstructor
public class ThreeGalarBirdsResponse extends ResearchResponse {

	private List<VersatilityIv> ivList;
	private int cp;
	private boolean wbFlg;
}
