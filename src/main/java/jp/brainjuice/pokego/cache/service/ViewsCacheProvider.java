package jp.brainjuice.pokego.cache.service;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jp.brainjuice.pokego.business.dao.entity.GoPokedex;
import jp.brainjuice.pokego.business.service.utils.dto.SearchValue;
import jp.brainjuice.pokego.cache.inmemory.ViewTempList;
import jp.brainjuice.pokego.cache.inmemory.data.PageNameEnum;

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

	@Autowired
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
	@AfterReturning("execution(* jp.brainjuice.pokego.business.service.ResearchService.exec(..))")
	public void addTempList(JoinPoint jp) {

		if (((SearchValue) jp.getArgs()[0]).isEnableCount()) {
			// enableCountがオンの場合は閲覧数をカウントしない。
			return;
		}

		// HttpServletRequestから、page(SearchPattern)とIPアドレスを取得する。
		HttpServletRequest req = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
		String uri = req.getRequestURI();
		String page = uri.substring(uri.lastIndexOf("/") + 1, uri.length());
		String ip = req.getRemoteAddr();

		GoPokedex goPokedex = ((SearchValue) jp.getArgs()[0]).getGoPokedex();
		String pokedexId = goPokedex.getPokedexId();

		addTempList(PageNameEnum.valueOf(page), pokedexId, ip);
	}

	/**
	 * 閲覧情報をメモリ上のリストに追加する。<br>
	 * 単体のポケモンに対する閲覧じゃない場合の呼び出し口。
	 *
	 */
	public void addTempList() {

		// HttpServletRequestから、page(SearchPattern)とIPアドレスを取得する。
		HttpServletRequest req = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
		String uri = req.getRequestURI();
		String page = uri.substring(uri.lastIndexOf("/") + 1, uri.length());
		String ip = req.getRemoteAddr();

		addTempList(PageNameEnum.valueOf(page), null, ip);

	}

	/**
	 * 閲覧情報をメモリ上のリストに追加する。<br>
	 * 単体のポケモンに対する閲覧じゃない場合の呼び出し口。
	 *
	 */
	public void addTempList(String pokedexId) {

		// HttpServletRequestから、page(SearchPattern)とIPアドレスを取得する。
		HttpServletRequest req = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
		String uri = req.getRequestURI();
		String page = uri.substring(uri.lastIndexOf("/") + 1, uri.length());
		String ip = req.getRemoteAddr();

		addTempList(PageNameEnum.valueOf(page), pokedexId, ip);

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
		viewsTempList.add(page, pokedexId, ip);

	}

	/**
	 * ページごとの閲覧数をRedisサーバからすべて取得します。
	 *
	 * @return
	 */
	public Map<PageNameEnum, Integer> findPageViewsAll() {

		Map<PageNameEnum, Integer> rtnMap = viewsCacheManager.findPageViewsAll();
		return rtnMap;
	}

	/**
	 * ポケモンごとの閲覧数をRedisサーバからすべて取得します。
	 *
	 * @return
	 */
	public Map<String, Integer> findPokemonViewsAll() {

		Map<String, Integer> rtnMap = viewsCacheManager.findPokemonViewsAll();
		return rtnMap;
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

}
