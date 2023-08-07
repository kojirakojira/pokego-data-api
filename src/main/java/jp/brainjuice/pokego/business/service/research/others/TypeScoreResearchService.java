package jp.brainjuice.pokego.business.service.research.others;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jp.brainjuice.pokego.business.constant.Type.TypeEnum;
import jp.brainjuice.pokego.business.dao.entity.GoPokedex;
import jp.brainjuice.pokego.business.service.research.ResearchService;
import jp.brainjuice.pokego.business.service.utils.dto.SearchValue;
import jp.brainjuice.pokego.business.service.utils.memory.TypeChartInfo;
import jp.brainjuice.pokego.business.service.utils.memory.TypeCommentMap;
import jp.brainjuice.pokego.web.form.res.research.others.TypeScoreResponse;

@Service
public class TypeScoreResearchService implements ResearchService<TypeScoreResponse> {

	TypeChartInfo typeChartInfo;

	TypeCommentMap typeCommentMap;

	@Autowired
	public TypeScoreResearchService(TypeChartInfo typeChartInfo, TypeCommentMap typeCommentMap) {
		this.typeChartInfo = typeChartInfo;
		this.typeCommentMap = typeCommentMap;
	}

	@Override
	public void exec(SearchValue sv, TypeScoreResponse res) {
		GoPokedex goPokedex = sv.getGoPokedex();
		TypeEnum type1 = TypeEnum.getType(goPokedex.getType1());
		TypeEnum type2 = TypeEnum.getType(goPokedex.getType2());

		exec(type1, type2, res);
		res.setExecutedType(false); // ポケモンから実行
	}

	/**
	 * タイプから検索したときの呼び口
	 *
	 * @param type1
	 * @param type2
	 * @param res
	 */
	public void execFromType(TypeEnum type1, TypeEnum type2, TypeScoreResponse res) {
		exec(type1, type2, res);
		res.setSuccess(true);
		res.setExecutedType(true); // タイプから実行
	}

	public void exec(TypeEnum type1, TypeEnum type2, TypeScoreResponse res) {

		// タイプ1がnullの場合、タイプ2をタイプ1に設定する。
		if (type1 == null && type2 != null) {
			type1 = type2;
			type2 = null;
		}

		// タイプ名
		res.setType1(type1);
		res.setType2(type2);

		// 評価
		res.setAttacker1Score(typeChartInfo.attackerScore(type1));
		if (type2 != null) {
			res.setAttacker2Score(typeChartInfo.attackerScore(type2));
		}
		res.setDefenderScore(typeChartInfo.defenderScore(type1, type2));

		// 倍率ごとのタイプリスト
		res.setAttackerType1Map(typeChartInfo.getAttackerTypes(type1));
		if (type2 != null) {
			res.setAttackerType2Map(typeChartInfo.getAttackerTypes(type2));
		}
		res.setDefenderTypeMap(typeChartInfo.getDefenderTypes(type1, type2));

		// タイプコメント
		res.setTypeComments(typeCommentMap.get(type1, type2));

		res.setMessage("");
	}

}
