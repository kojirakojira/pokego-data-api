package jp.brainjuice.pokego.web;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jp.brainjuice.pokego.business.service.GoConvertService;
import jp.brainjuice.pokego.business.service.PokemonSearchService;
import jp.brainjuice.pokego.business.service.RaceDiffService;
import jp.brainjuice.pokego.business.service.UnimplPokemonService;
import jp.brainjuice.pokego.business.service.research.RaceResearchService;
import jp.brainjuice.pokego.business.service.research.ResearchServiceExecutor;
import jp.brainjuice.pokego.business.service.utils.dto.MultiSearchResult;
import jp.brainjuice.pokego.business.service.utils.dto.PokemonSearchResult;
import jp.brainjuice.pokego.cache.inmemory.TopicPageList;
import jp.brainjuice.pokego.cache.inmemory.TopicPokemonList;
import jp.brainjuice.pokego.cache.service.TopicListProvider;
import jp.brainjuice.pokego.utils.exception.BadRequestException;
import jp.brainjuice.pokego.web.form.req.RaceDiffRequest;
import jp.brainjuice.pokego.web.form.req.research.RaceRequest;
import jp.brainjuice.pokego.web.form.res.RaceDiffResponse;
import jp.brainjuice.pokego.web.form.res.UnimplPokemonResponse;
import jp.brainjuice.pokego.web.form.res.elem.SimpPokemon;
import jp.brainjuice.pokego.web.form.res.research.RaceResponse;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api")
@Slf4j
public class SearchController {

	private RaceResearchService raceResearchService;
	private ResearchServiceExecutor<RaceResponse> raceResRse;

	private RaceDiffService raceDiffService;

	private GoConvertService goConvertService;

	private PokemonSearchService pokemonSearchService;

	private TopicListProvider topicListProvider;

	private UnimplPokemonService unimplPokemonService;

	@Autowired
	public SearchController(
			RaceResearchService raceResearchService, ResearchServiceExecutor<RaceResponse> raceResRse,
			RaceDiffService raceDiffService,
			GoConvertService goConvertService,
			PokemonSearchService pokemonSearchService,
			TopicListProvider topicListProvider,
			UnimplPokemonService unimplPokemonService) {
		// 種族値検索
		this.raceResearchService = raceResearchService;
		this.raceResRse = raceResRse;
		// 種族値比較
		this.raceDiffService = raceDiffService;

		this.goConvertService = goConvertService;
		// 検索
		this.pokemonSearchService = pokemonSearchService;

		this.topicListProvider = topicListProvider;

		this.unimplPokemonService = unimplPokemonService;
	}

	@GetMapping("/home")
	public String home() {
		return "pokego-api server is running!!";
	}

	@GetMapping("/go")
	public String go(String id) {

		return goConvertService.getGoStatus(id);
	}

	@GetMapping("/save")
	public String save() {

		return goConvertService.insertGoPokedexAll();
	}

	@GetMapping("/search")
	public PokemonSearchResult search(String name) {
		return pokemonSearchService.search(name);
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
	 * 種族値比較用API（content-type:application/jsonで取得する。）
	 *
	 * @param raceReq
	 * @return
	 * @throws BadRequestException
	 */
	@PostMapping("/raceDiff")
	public RaceDiffResponse raceDiff(@RequestBody RaceDiffRequest raceDiffReq) throws BadRequestException {

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

		return res;
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
