package jp.brainjuice.pokego.web.form.res.cp;

import java.util.List;

import jp.brainjuice.pokego.business.constant.SituationEnum;
import jp.brainjuice.pokego.web.form.res.ResearchResponse;
import jp.brainjuice.pokego.web.form.res.elem.VersatilityIv;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper=false)
@NoArgsConstructor
public class CpIvResponse extends ResearchResponse {

	private SituationEnum situation;
	private List<VersatilityIv> ivList;
	private int cp;
	private boolean wbFlg;
}
