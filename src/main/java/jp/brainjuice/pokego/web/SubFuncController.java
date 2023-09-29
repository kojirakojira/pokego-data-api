package jp.brainjuice.pokego.web;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jp.brainjuice.pokego.business.service.sub.OgpInfoService;
import jp.brainjuice.pokego.business.service.sub.PrevNextPokemonService;
import jp.brainjuice.pokego.cache.inmemory.TopicPageList;
import jp.brainjuice.pokego.cache.inmemory.TopicPokemonList;
import jp.brainjuice.pokego.cache.service.TopicListProvider;
import jp.brainjuice.pokego.cache.service.ViewsCacheProvider;
import jp.brainjuice.pokego.utils.exception.BadRequestException;
import jp.brainjuice.pokego.web.form.req.sub.OgpPokemonRequest;
import jp.brainjuice.pokego.web.form.req.sub.OgpTypeRequest;
import jp.brainjuice.pokego.web.form.req.sub.PrevNextPokemonRequest;
import jp.brainjuice.pokego.web.form.res.sub.OgpPokemonResponse;
import jp.brainjuice.pokego.web.form.res.sub.OgpTypeResponse;
import jp.brainjuice.pokego.web.form.res.sub.PrevNextPokemonResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * 補助機能にアクセスするためのコントローラクラス
 *
 * @author saibabanagchampa
 *
 */
@RestController
@RequestMapping("/api")
@Slf4j
public class SubFuncController {

	private PrevNextPokemonService prevNextPokemonService;

	private OgpInfoService ogpInfoService;

	private TopicListProvider topicListProvider;

	private ViewsCacheProvider viewsCacheProvider;

	@Autowired
	public SubFuncController(
			PrevNextPokemonService prevNextPokemonService,
			OgpInfoService ogpInfoService,
			TopicListProvider topicListProvider,
			ViewsCacheProvider viewsCacheProvider) {

		this.prevNextPokemonService = prevNextPokemonService;

		this.ogpInfoService = ogpInfoService;

		this.topicListProvider = topicListProvider;

		this.viewsCacheProvider = viewsCacheProvider;

	}

	@GetMapping("/test")
	public String home() {
		return "pokego-api server is running!!";
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

		OgpTypeResponse ogpTypeRes = new OgpTypeResponse();

		if (!StringUtils.isEmpty(ogpTypeReq.getType1()) || !StringUtils.isEmpty(ogpTypeReq.getType2())) {
			// タイプから検索する場合
			ogpInfoService.execTypeInfo(ogpTypeReq.getType1(), ogpTypeReq.getType2(), ogpTypeRes);
		} else if (!StringUtils.isEmpty(ogpTypeReq.getId())) {
			// pokedexIdから検索する場合
			ogpInfoService.execTypeInfo(ogpTypeReq.getId(), ogpTypeRes);
		}

		return ogpTypeRes;
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
