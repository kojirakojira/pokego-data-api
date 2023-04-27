package jp.brainjuice.pokego.web;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jp.brainjuice.pokego.business.constant.Type.TypeEnum;
import jp.brainjuice.pokego.business.service.XTypeService;
import jp.brainjuice.pokego.business.service.research.ResearchServiceExecutor;
import jp.brainjuice.pokego.business.service.research.others.TypeScoreResearchService;
import jp.brainjuice.pokego.cache.service.ViewsCacheProvider;
import jp.brainjuice.pokego.utils.exception.BadRequestException;
import jp.brainjuice.pokego.web.form.req.XTypeRequest;
import jp.brainjuice.pokego.web.form.req.research.others.TypeScoreRequest;
import jp.brainjuice.pokego.web.form.res.XTypeResponse;
import jp.brainjuice.pokego.web.form.res.research.others.TypeScoreResponse;

@RestController
@RequestMapping("/api")
public class TypeController {

	private TypeScoreResearchService typeScoreResearchService;
	private ResearchServiceExecutor<TypeScoreResponse> typeScoreResRse;

	private XTypeService xTypeService;

	private ViewsCacheProvider viewsCacheProvider;

	public TypeController (
			TypeScoreResearchService typeScoreResearchService, ResearchServiceExecutor<TypeScoreResponse> typeScoreResRse,
			XTypeService xTypeService,
			ViewsCacheProvider viewsCacheProvider) {

		// タイプ評価
		this.typeScoreResearchService = typeScoreResearchService;
		this.typeScoreResRse = typeScoreResRse;

		// Xタイプ検索
		this.xTypeService = xTypeService;

		this.viewsCacheProvider = viewsCacheProvider;
	}

	/**
	 * タイプ評価取得用API
	 *
	 * @param typeScoreReq
	 * @return
	 * @throws BadRequestException
	 */
	@GetMapping("/typeScore")
	public TypeScoreResponse typeScore(TypeScoreRequest typeScoreReq) throws BadRequestException {

		TypeScoreResponse typeScoreRes = new TypeScoreResponse();
		if (StringUtils.isAllEmpty(typeScoreReq.getType1(), typeScoreReq.getType2())) {
			// type1, type2がnull(または空)の場合は、id or nameで検索する。
			typeScoreResRse.execute(typeScoreReq, typeScoreRes, typeScoreResearchService);
		} else {
			TypeEnum type1 = StringUtils.isEmpty(typeScoreReq.getType1()) ? null: TypeEnum.valueOf(typeScoreReq.getType1());
			TypeEnum type2 = StringUtils.isEmpty(typeScoreReq.getType2()) ? null: TypeEnum.valueOf(typeScoreReq.getType2());
			typeScoreResearchService.execFromType(type1, type2, typeScoreRes);

			// 閲覧数を手動で追加。
			viewsCacheProvider.addTempList();
		}
		return typeScoreRes;
	}

	/**
	 * Xタイプ検索用API
	 *
	 * @param xTypeReq
	 * @return
	 * @throws BadRequestException
	 */
	@GetMapping("/xType")
	public XTypeResponse xType(XTypeRequest xTypeReq) throws BadRequestException {

		XTypeResponse xTypeRes = new XTypeResponse();

		String ownType1 = xTypeReq.getOwn1();
		String ownType2 = xTypeReq.getOwn2();
		String oppType1 = xTypeReq.getOpp1();
		String oppType2 = xTypeReq.getOpp2();
		String emphasis = xTypeReq.getEmphasis();

		if (!xTypeService.check(ownType1, ownType2, oppType1, oppType2, emphasis, xTypeRes)) {
			return xTypeRes;
		}

		xTypeService.exec(ownType1, ownType2, oppType1, oppType2, emphasis, xTypeRes);

		// 閲覧数を手動で追加。
		viewsCacheProvider.addTempList();

		return xTypeRes;
	}

}
