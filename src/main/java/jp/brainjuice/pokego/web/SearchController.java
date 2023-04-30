package jp.brainjuice.pokego.web;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jp.brainjuice.pokego.business.service.OgpInfoService;
import jp.brainjuice.pokego.business.service.PokemonSearchService;
import jp.brainjuice.pokego.business.service.RaceDiffService;
import jp.brainjuice.pokego.business.service.UnimplPokemonService;
import jp.brainjuice.pokego.business.service.utils.dto.MultiSearchResult;
import jp.brainjuice.pokego.business.service.utils.dto.PokemonFilterResult;
import jp.brainjuice.pokego.business.service.utils.dto.PokemonSearchResult;
import jp.brainjuice.pokego.cache.inmemory.TopicPageList;
import jp.brainjuice.pokego.cache.inmemory.TopicPokemonList;
import jp.brainjuice.pokego.cache.service.TopicListProvider;
import jp.brainjuice.pokego.cache.service.ViewsCacheProvider;
import jp.brainjuice.pokego.utils.exception.BadRequestException;
import jp.brainjuice.pokego.web.form.req.OgpPokemonRequest;
import jp.brainjuice.pokego.web.form.req.OgpTypeRequest;
import jp.brainjuice.pokego.web.form.req.RaceDiffRequest;
import jp.brainjuice.pokego.web.form.req.research.FilterAllRequest;
import jp.brainjuice.pokego.web.form.req.research.SearchAllRequest;
import jp.brainjuice.pokego.web.form.res.FilterAllResponse;
import jp.brainjuice.pokego.web.form.res.OgpPokemonResponse;
import jp.brainjuice.pokego.web.form.res.OgpTypeResponse;
import jp.brainjuice.pokego.web.form.res.RaceDiffResponse;
import jp.brainjuice.pokego.web.form.res.SearchAllResponse;
import jp.brainjuice.pokego.web.form.res.UnimplPokemonResponse;
import jp.brainjuice.pokego.web.form.res.elem.SimpPokemon;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api")
@Slf4j
public class SearchController {

	private RaceDiffService raceDiffService;

	private PokemonSearchService pokemonSearchService;

	private TopicListProvider topicListProvider;

	private UnimplPokemonService unimplPokemonService;

	private OgpInfoService ogpInfoService;

	private ViewsCacheProvider viewsCacheProvider;

	@Autowired
	public SearchController(
			RaceDiffService raceDiffService,
			PokemonSearchService pokemonSearchService,
			TopicListProvider topicListProvider,
			UnimplPokemonService unimplPokemonService,
			OgpInfoService ogpInfoService,
			ViewsCacheProvider viewsCacheProvider) {

		// 種族値比較
		this.raceDiffService = raceDiffService;

		// 検索
		this.pokemonSearchService = pokemonSearchService;

		this.topicListProvider = topicListProvider;

		this.unimplPokemonService = unimplPokemonService;

		this.ogpInfoService = ogpInfoService;

		this.viewsCacheProvider = viewsCacheProvider;
	}

	@GetMapping("/home")
	public String home() {
		return "pokego-api server is running!!";
	}

	/**
	 * ポケモン検索用API
	 *
	 * @param name
	 * @return
	 */
	@GetMapping("/searchAll")
	public SearchAllResponse search(SearchAllRequest req) {

		SearchAllResponse res = new SearchAllResponse();
		PokemonSearchResult psr = pokemonSearchService.search(req.getName());
		res.setSuccess(true);
		res.setMessage("");
		res.setPokemonSearchResult(psr);

		// 閲覧数を手動で追加。
		viewsCacheProvider.addTempList();

		return res;
	}

	/**
	 * 絞り込み検索用API
	 *
	 * @param req
	 * @return
	 */
	@GetMapping("/filterAll")
	public FilterAllResponse filterAll(FilterAllRequest req) {

		FilterAllResponse res = new FilterAllResponse();
		PokemonFilterResult pfr = pokemonSearchService.filter(req);
		res.setPfr(pfr);
		res.setSuccess(true);
		res.setMessage("");

		// 閲覧数を手動で追加。
		viewsCacheProvider.addTempList();

		return res;
	}

	/**
	 * 種族値比較用API（content-type:application/jsonで取得する。）
	 *
	 * @param raceReq
	 * @return
	 * @throws BadRequestException
	 */
	@GetMapping("/raceDiff")
	public RaceDiffResponse raceDiff(RaceDiffRequest raceDiffReq) throws BadRequestException {

		RaceDiffResponse raceDiffRes = new RaceDiffResponse();
		if (!raceDiffService.check(raceDiffReq, raceDiffRes)) {
			return raceDiffRes;
		}

		if (raceDiffReq.getIdArr() != null) {
			// idでの検索
			raceDiffService.exec(raceDiffReq, raceDiffRes);

		} else {

			MultiSearchResult msr = pokemonSearchService.multiSearch(raceDiffReq.getNameArr());
			raceDiffRes.setMsr(msr);
			raceDiffRes.setSuccess(true);

			if (msr.isAllUnique()) {
				// MultiSearchResult
				raceDiffService.exec(msr, raceDiffRes);
			}
		}

		// 閲覧数を手動で追加。
		viewsCacheProvider.addTempList();

		return raceDiffRes;
	}


	/**
	 * 話題のページ取得用API
	 *
	 * @return
	 */
	@GetMapping("/topicPage")
	public TopicPageList topicPage() {

		return topicListProvider.getTopicPageList();
	}

	/**
	 * 話題のポケモン取得用API
	 *
	 * @return
	 */
	@GetMapping("/topicPokemon")
	public TopicPokemonList topicPokemon() {

		return topicListProvider.getTopicPokemonList();
	}

	/**
	 * 話題の○○一覧を強制的に更新するAPI
	 *
	 * @return
	 */
	@GetMapping("/updateTopicList")
	public String updateTopicList() {

		topicListProvider.updateTopicList();
		return "成功！";
	}

	/**
	 * 未実装ポケモン一覧取得用API
	 *
	 * @return
	 */
	@GetMapping("/unimplPokemon")
	public UnimplPokemonResponse unimplPokemon() {

		UnimplPokemonResponse res = new UnimplPokemonResponse();
		List<SimpPokemon> simpPokemonList = unimplPokemonService.getUnimplementedPokemonList();
		res.setUnimplList(simpPokemonList);
		res.setSuccess(true);
		res.setMessage("");

		// 閲覧数を手動で追加。
		viewsCacheProvider.addTempList();

		return res;
	}

	/**
	 * OGP情報の取得（ポケモン情報）
	 *
	 * @return
	 */
	@GetMapping("/ogpPokemon")
	public OgpPokemonResponse ogpPokemon(OgpPokemonRequest ogpPokemonReq) {

		OgpPokemonResponse ogpPokemonRes = new OgpPokemonResponse();

		ogpInfoService.execPokemonInfo(ogpPokemonReq.getId(), ogpPokemonRes);

		return ogpPokemonRes;
	}

	/**
	 * OGP情報の取得（タイプ情報）
	 *
	 * @return
	 */
	@GetMapping("/ogpType")
	public OgpTypeResponse ogpType(OgpTypeRequest ogpTypeReq) {

		OgpTypeResponse ogpPokemonRes = new OgpTypeResponse();

		ogpInfoService.execTypeInfo(ogpTypeReq.getType1(), ogpTypeReq.getType2(), ogpPokemonRes);

		return ogpPokemonRes;
	}

	@ExceptionHandler(BadRequestException.class)
	public ResponseEntity<String> badRequestException(Exception e) {
		String errMsg = "不正なアクセスです。";
		log.error(errMsg, e);
		return new ResponseEntity<String>(errMsg, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<String> exception(Exception e) {
		String errMsg = "処理中に想定外の問題が発生しました。";
		log.error(errMsg, e);
		return new ResponseEntity<String>(errMsg, HttpStatus.INTERNAL_SERVER_ERROR);
	}

}
