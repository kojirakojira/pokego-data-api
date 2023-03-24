package jp.brainjuice.pokego.web.form.res.research.others;

import jp.brainjuice.pokego.business.dao.entity.GoPokedex;
import jp.brainjuice.pokego.web.form.res.elem.Color;
import jp.brainjuice.pokego.web.form.res.research.ResearchResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class AbundanceResponse extends ResearchResponse {

	private GoPokedex goPokedex;
	// PL40時のcp
	private int cp40;
	// CP
	private int maxCp;
	// レイド
	private int minRaidCp;
	private int maxRaidCp;
	private int minWbRaidCp;
	private int maxWbRaidCp;
	// シャドウ
	private int minShadowCp;
	private int maxShadowCp;
	private int minWbShadowCp;
	private int maxWbShadowCp;
	// フィールドリサーチ
	private int minFrTaskCp;
	private int maxFrTaskCp;
	// 強ポケ補正対象か否か
	private boolean tooStrong;

	// タイプから算出したポケモンの色
	private Color type1Color;
	private Color type2Color;
}
