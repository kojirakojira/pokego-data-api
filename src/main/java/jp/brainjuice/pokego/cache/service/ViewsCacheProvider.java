package jp.brainjuice.pokego.cache.service;

import jakarta.servlet.http.HttpServletRequest;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Collections;
import java.util.List;

import jp.brainjuice.pokego.business.service.search.pokeFilter.dto.SearchValue;
import jp.brainjuice.pokego.cache.inmemory.topic.ViewTempList;
import jp.brainjuice.pokego.cache.inmemory.topic.data.PageNameEnum;
import jp.brainjuice.pokego.dao.jpa.entity.GoPokedex;

/**
 * 閲覧情報にアクセスするためのプロバイダクラスです。
 * 閲覧情報は、メモリ(ViewTempInfo)→Redisサーバ(一時的にPage,Pokemonを追加)→メモリ(TopicPageとTopicPokemon)と処理されていきます。
 *
 * 閲覧情報を処理する目的は大きく2つあります。
 * 1つ目は、Topic機能で直近のアクセス上位を集計することです。
 * 2つ目は、ページ、ポケモン単位でトータルの閲覧数を集計することです。
 *
 * @author saibabanagchampa
 * @see ViewsCacheManager
 *
 */
@Service
@Aspect
public class ViewsCacheProvider {

	private ViewsCacheManager viewsCacheManager;

	public ViewsCacheProvider(
			ViewsCacheManager viewsCacheManager) {
		this.viewsCacheManager = viewsCacheManager;
	}

	/**
	 * 閲覧情報をメモリ上のリストに追加する。<br>
	 * ResearchService継承クラスのexecメソッド実行後、正常終了した場合に割り込む。
	 *
	 * @param jp
	 */
	@AfterReturning("execution(* jp.brainjuice.pokego.business.service.search.ResearchService.exec(..))")
	public void addTempList(JoinPoint jp) {

		if (((SearchValue) jp.getArgs()[0]).isEnableCount()) {
			// enableCountがオンの場合は閲覧数をカウントしない。
			return;
		}

		// HttpServletRequestから、page(SearchPattern)とIPアドレスを取得する。
		HttpServletRequest req = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
				.getRequest();
		String uri = req.getRequestURI();
		String page = uri.substring(uri.lastIndexOf("/") + 1, uri.length());
		String ip = req.getRemoteAddr();

		GoPokedex goPokedex = ((SearchValue) jp.getArgs()[0]).getGoPokedex();
		String pokedexId = goPokedex.getPokedexId();

		addTempList(PageNameEnum.valueOf(page), pokedexId, ip);
	}

	/**
	 * 閲覧情報をメモリ上のリストに追加する。<br>
	 * 単一のポケモンに限定できないパターンの場合の呼び出し口
	 *
	 */
	public void addTempList() {

		// HttpServletRequestから、page(SearchPattern)とIPアドレスを取得する。
		HttpServletRequest req = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
				.getRequest();
		String uri = req.getRequestURI();
		String page = uri.substring(uri.lastIndexOf("/") + 1, uri.length());
		String ip = req.getRemoteAddr();

		addTempList(PageNameEnum.valueOf(page), (String) null, ip);

	}

	/**
	 * 閲覧情報をメモリ上のリストに追加する。<br>
	 * 単体のポケモンに対する閲覧じゃない場合の呼び出し口。
	 *
	 */
	public void addTempList(String pokedexId) {

		// HttpServletRequestから、page(SearchPattern)とIPアドレスを取得する。
		HttpServletRequest req = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
				.getRequest();
		String uri = req.getRequestURI();
		String page = uri.substring(uri.lastIndexOf("/") + 1, uri.length());
		String ip = req.getRemoteAddr();

		addTempList(PageNameEnum.valueOf(page), pokedexId, ip);

	}

	/**
	 * 閲覧情報をメモリ上のリストに追加する。<br>
	 * 複数ポケモンに対する閲覧の場合の呼び出し口。
	 *
	 */
	public void addTempList(List<String> pokedexIds) {

		// HttpServletRequestから、page(SearchPattern)とIPアドレスを取得する。
		HttpServletRequest req = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
				.getRequest();
		String uri = req.getRequestURI();
		String page = uri.substring(uri.lastIndexOf("/") + 1, uri.length());
		String ip = req.getRemoteAddr();

		addTempList(PageNameEnum.valueOf(page), pokedexIds, ip);

	}

	/**
	 * 閲覧情報をメモリ上のリストに追加する。
	 *
	 * @param page
	 * @param pokedexId
	 * @param ip
	 */
	public void addTempList(PageNameEnum page, String pokedexId, String ip) {

		ViewTempList viewsTempList = viewsCacheManager.getViewsTempList();
		viewsTempList.add(page, pokedexId == null ? null : Collections.singletonList(pokedexId), ip);

	}

	/**
	 * 閲覧情報をメモリ上のリストに追加する。（複数ポケモン用）
	 *
	 * @param page
	 * @param pokedexIds
	 * @param ip
	 */
	public void addTempList(PageNameEnum page, List<String> pokedexIds, String ip) {

		ViewTempList viewsTempList = viewsCacheManager.getViewsTempList();
		viewsTempList.add(page, pokedexIds, ip);

	}

	/**
	 * この{@link ViewsCacheManager#cleanupPageTempView() メソッド}を参照
	 * 
	 * @param pid
	 * @return
	 */
	public void cleanupPageTempView() {
		viewsCacheManager.cleanupPageTempView();
	}

	/**
	 * この{@link ViewsCacheManager#cleanupPokemonTempView() メソッド}を参照
	 * 
	 * @param pid
	 * @return
	 */
	public void cleanupPokemonTempView() {
		viewsCacheManager.cleanupPokemonTempView();
	}

	/**
	 * リリース後、古いキャッシュが残り不備が発生する場合、このAPIを使用する。（ページ一時情報用）
	 */
	public void clearPageTempView() {

		viewsCacheManager.clearPageTempView();
	}

	/**
	 * リリース後、古いキャッシュが残り不備が発生する場合、このAPIを使用する。（ポケモン一時情報用）
	 */
	public void clearPokemonTempView() {

		viewsCacheManager.clearPokemonTempView();
	}

	/**
	 * この{@link ViewsCacheManager#cleanupRaceDiffSearchTempView() メソッド}を参照
	 */
	public void cleanupRaceDiffSearchTempView() {
		viewsCacheManager.cleanupRaceDiffSearchTempView();
	}

	/**
	 * リリース後、古いキャッシュが残り不備が発生する場合、このAPIを使用する。（種族値比較一時情報用）
	 */
	public void clearRaceDiffSearchTempView() {
		viewsCacheManager.clearRaceDiffSearchTempView();
	}

}
