package jp.brainjuice.pokego.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jp.brainjuice.pokego.business.service.general.PokemonSearchService;
import jp.brainjuice.pokego.business.service.others.UnimplPokemonService;
import jp.brainjuice.pokego.business.service.race.RaceDiffService;
import jp.brainjuice.pokego.business.service.sub.OgpInfoService;
import jp.brainjuice.pokego.business.service.sub.PrevNextPokemonService;
import jp.brainjuice.pokego.business.service.utils.dto.MultiSearchResult;
import jp.brainjuice.pokego.business.service.utils.dto.PokemonFilterResult;
import jp.brainjuice.pokego.business.service.utils.dto.PokemonSearchResult;
import jp.brainjuice.pokego.cache.inmemory.TopicPageList;
import jp.brainjuice.pokego.cache.inmemory.TopicPokemonList;
import jp.brainjuice.pokego.cache.service.TopicListProvider;
import jp.brainjuice.pokego.cache.service.ViewsCacheProvider;
import jp.brainjuice.pokego.utils.exception.BadRequestException;
import jp.brainjuice.pokego.web.form.req.general.FilterAllRequest;
import jp.brainjuice.pokego.web.form.req.general.SearchAllRequest;
import jp.brainjuice.pokego.web.form.req.race.RaceDiffRequest;
import jp.brainjuice.pokego.web.form.req.sub.OgpPokemonRequest;
import jp.brainjuice.pokego.web.form.req.sub.OgpTypeRequest;
import jp.brainjuice.pokego.web.form.req.sub.PrevNextPokemonRequest;
import jp.brainjuice.pokego.web.form.res.general.FilterAllResponse;
import jp.brainjuice.pokego.web.form.res.general.SearchAllResponse;
import jp.brainjuice.pokego.web.form.res.others.UnimplPokemonResponse;
import jp.brainjuice.pokego.web.form.res.race.RaceDiffResponse;
import jp.brainjuice.pokego.web.form.res.sub.OgpPokemonResponse;
import jp.brainjuice.pokego.web.form.res.sub.OgpTypeResponse;
import jp.brainjuice.pokego.web.form.res.sub.PrevNextPokemonResponse;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api")
@Slf4j
public class SearchController {

	private RaceDiffService raceDiffService;

	private PokemonSearchService pokemonSearchService;

	private TopicListProvider topicListProvider;

	private UnimplPokemonService unimplPokemonService;

	private PrevNextPokemonService prevNextPokemonService;

	private OgpInfoService ogpInfoService;

	private ViewsCacheProvider viewsCacheProvider;

	@Autowired
	public SearchController(
			RaceDiffService raceDiffService,
			PokemonSearchService pokemonSearchService,
			TopicListProvider topicListProvider,
			UnimplPokemonService unimplPokemonService,
			PrevNextPokemonService prevNextPokemonService,
			OgpInfoService ogpInfoService,
			ViewsCacheProvider viewsCacheProvider) {

		// 種族値比較
		this.raceDiffService = raceDiffService;

		// 検索
		this.pokemonSearchService = pokemonSearchService;

		this.topicListProvider = topicListProvider;

		this.unimplPokemonService = unimplPokemonService;

		this.prevNextPokemonService = prevNextPokemonService;

		this.ogpInfoService = ogpInfoService;

		this.viewsCacheProvider = viewsCacheProvider;
	}

	@GetMapping("/test")
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
		unimplPokemonService.exec(res);

		// 閲覧数を手動で追加。
		viewsCacheProvider.addTempList();

		return res;
	}

	/**
	 * 図鑑ID上の1つ前、1つ後のポケモンのGoPokedexを取得するAPI
	 * @param req
	 * @return
	 */
	@GetMapping("/prevNextPokemon")
	public PrevNextPokemonResponse prevNextPokemon(PrevNextPokemonRequest req) {

		PrevNextPokemonResponse res = new PrevNextPokemonResponse();
		prevNextPokemonService.exec(req.getId(), res);

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

	/**
	 * Redisサーバ上のページの一時情報をすべて削除する。
	 * TODO: いずれsecureにしようと思うが、悪影響がないので一旦解放。
	 *
	 * @return
	 */
	@GetMapping("/clearPageTempView")
	public String clearPageTempView() {

		viewsCacheProvider.clearPageTempView();

		return "成功";
	}


	/**
	 * Redisサーバ上のポケモンの一時情報をすべて削除する。
	 * TODO: いずれsecureにしようと思うが、悪影響がないので一旦解放。
	 *
	 * @return
	 */
	@GetMapping("/clearPokemonTempView")
	public String clearPokemonTempView() {

		viewsCacheProvider.clearPokemonTempView();

		return "成功";
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
