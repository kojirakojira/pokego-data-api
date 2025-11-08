package jp.brainjuice.pokego.web.search;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jp.brainjuice.pokego.business.service.search.ResearchServiceExecutor;
import jp.brainjuice.pokego.business.service.search.general.PokemonSearchService;
import jp.brainjuice.pokego.business.service.search.race.RaceDiffService;
import jp.brainjuice.pokego.business.service.search.race.RaceResearchService;
import jp.brainjuice.pokego.business.service.search.utils.dto.MultiSearchResult;
import jp.brainjuice.pokego.cache.service.ViewsCacheProvider;
import jp.brainjuice.pokego.utils.exception.BadRequestException;
import jp.brainjuice.pokego.web.search.form.req.race.RaceDiffRequest;
import jp.brainjuice.pokego.web.search.form.req.race.RaceRequest;
import jp.brainjuice.pokego.web.search.form.res.elem.PidAndName;
import jp.brainjuice.pokego.web.search.form.res.race.RaceDiffResponse;
import jp.brainjuice.pokego.web.search.form.res.race.RaceResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * 種族値情報を取得するためのコントローラクラス
 *
 * @author saibabanagchampa
 *
 */
@RestController
@RequestMapping("/api")
@Slf4j
public class RaceController {

	private PokemonSearchService pokemonSearchService;

	private RaceResearchService raceResearchService;
	private ResearchServiceExecutor<RaceResponse> raceResRse;

	private RaceDiffService raceDiffService;

	private ViewsCacheProvider viewsCacheProvider;

	public RaceController(
			RaceDiffService raceDiffService,
			RaceResearchService raceResearchService, ResearchServiceExecutor<RaceResponse> raceResRse,
			PokemonSearchService pokemonSearchService,
			ViewsCacheProvider viewsCacheProvider) {

		// 検索
		this.pokemonSearchService = pokemonSearchService;

		// 種族値検索
		this.raceResearchService = raceResearchService;
		this.raceResRse = raceResRse;

		// 種族値比較
		this.raceDiffService = raceDiffService;

		this.viewsCacheProvider = viewsCacheProvider;

	}

	/**
	 * 種族値検索用API
	 *
	 * @param raceReq
	 * @return
	 * @throws BadRequestException
	 */
	@GetMapping("/race")
	public RaceResponse race(RaceRequest raceReq) throws BadRequestException {

		RaceResponse raceRes = new RaceResponse();
		raceResRse.execute(raceReq, raceRes, raceResearchService);
		return raceRes;
	}

	/**
	 * 種族値比較用API（Content-Type:application/jsonで取得する。）<br>
	 * 仕様が少し複雑なため、以下に<span style="color:red;">主要な</span>パターンを示す。
	 * <ol>
	 *   <li>pid全部揃ってるパターン
	 *     <ul>
	 *       <li>→nameの有無に関わらず必ずid検索
	 *       <li>例：[{ id: "0001N01", name: "フシギダネ" }, { id: "0003N01", name: "" }]</li>
	 *       <li>idに重複がある場合
	 *         <ul>
	 *           <li>入力チェックエラーとして処理する。レスポンスのmsr.psrArr[i].messageでメッセージ内容を確認できる。</li>
	 *           <li>例：[{ id: "0001N01", name: "フシギダネ" }, { id: "0003N01", name: "" }, { id: "0003N01", name: "" }]</li>
	 *         </ul>
	 *       </li>
	 *     </ul>
	 *   </li>
	 *   <li>pid全部は揃ってないパターン
	 *     <ul>
	 *       <li>→name検索として処理する
	 *       <li>例：[{ id: "", name: "フシギダネ" }, { id: "", name: "フシギバナ" }]</li>
	 *       <li>idが存在している場合
	 *         <ul>
	 *           <li>idが存在しているものはname検索しない。以下の例の場合は、フシギバナだけ検索する</li>
	 *           <li>name検索後、すべて一意にポケモンを特定できたら、その後はid検索として振る舞う</li>
	 *           <li>例：[{ id: "0001N01", name: "" }, { id: "", name: "フシギバナ" }]</li>
	 *         </ul>
	 *       </li>
	 *     </ul>
	 *   </li>
	 * </ol>
	 *
	 * @param raceReq
	 * @return
	 * @throws BadRequestException
	 */
	@GetMapping("/raceDiff")
	public RaceDiffResponse raceDiff(RaceDiffRequest raceDiffReq) throws BadRequestException {

		RaceDiffResponse raceDiffRes = new RaceDiffResponse();
		if (!raceDiffService.checkBeforeNameSearch(raceDiffReq, raceDiffRes)) {
			return raceDiffRes;
		}

		List<PidAndName> pidAndNameList = raceDiffReq.getPidAndNameArr();

		if (!pidAndNameList.stream()
				.filter(pan -> StringUtils.isEmpty(pan.getPid()))
				.anyMatch(e -> true)) {
			// id検索の場合
			if (!raceDiffService.checkBeforeIdSearch(raceDiffReq, raceDiffRes)) {
				return raceDiffRes;
			}
			// pidがすべて揃っている場合は、pidでの検索
			raceDiffService.exec(raceDiffReq, raceDiffRes);

		} else {

			MultiSearchResult msr = pokemonSearchService.multiSearch(pidAndNameList);
			raceDiffRes.setMsr(msr);
			raceDiffRes.setSuccess(true);

			if (msr.isAllUnique()) {
				// 全てユニークの場合
				if (!raceDiffService.checkBeforeIdSearch(raceDiffReq, raceDiffRes)) {
					return raceDiffRes;
				}
				// すべてユニーク、かつポケモンに重複がない場合
				raceDiffService.exec(msr, raceDiffRes);
			}
		}

		// 閲覧数を手動で追加。
		viewsCacheProvider.addTempList();

		return raceDiffRes;
	}

}
