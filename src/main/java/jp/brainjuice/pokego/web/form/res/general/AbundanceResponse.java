package jp.brainjuice.pokego.web.form.res.general;

import java.util.List;

import jp.brainjuice.pokego.business.dao.entity.GoPokedex;
import jp.brainjuice.pokego.business.service.utils.dto.GoPokedexAndCpPl;
import jp.brainjuice.pokego.web.form.res.ResearchResponse;
import jp.brainjuice.pokego.web.form.res.elem.CatchCp;
import jp.brainjuice.pokego.web.form.res.elem.Color;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class AbundanceResponse extends ResearchResponse {

	private GoPokedex goPokedex;
	// CP(PL40)
	private int cp40;
	// CP(PL50)
	private int cp50;
	// CP(PL51)
	private int maxCp;
	// 野生
	private CatchCp wild;
	// レイド
	private CatchCp raid;
	// ロケット団
	private CatchCp rocket;
	// フィールドリサーチ
	private CatchCp frTask;
	// タマゴ
	private CatchCp egg;
	private GoPokedex eggGp;
	// ダイマックス、キョダイマックス
	private CatchCp dynamax;
	private CatchCp gigantamax;
	// 強ポケ補正対象か否か
	private boolean tooStrong;

	// タイプから算出したポケモンの色
	private Color type1Color;
	private Color type2Color;

	// メガシンカ（ゲンシカイキ含む）後か否か
	private boolean mega;
	// メガシンカ（ゲンシカイキ含む）可能か
	private boolean canMega;
	// メガシンカ可能かを示すメッセージ
	private String megaMsg;
	// ダイマックス可能かを示すメッセージ
	private String dynamaxMsg;
	// キョダイマックス可能かを示すメッセージ
	private String gigantamaxMsg;
	
	// そのポケモンを進化させた場合、最終進化後のポケモンのCPがリーグ制限に引っかからないギリギリのCP
	// ホゲータの場合、ラウドボーンがCP1500,2500にならないギリギリのCP
	private List<GoPokedexAndCpPl> superLeagueSafeCpList;
	private List<GoPokedexAndCpPl> hyperLeagueSafeCpList;
}
