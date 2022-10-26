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
import jp.brainjuice.pokego.business.service.utils.dto.IndividialValue;
import jp.brainjuice.pokego.cache.inmemory.ViewTempList;

/**
 * 閲覧情報にアクセスするためのプロバイダクラスです。
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
	@AfterReturning("execution(* jp.brainjuice.pokego.business.service.research.ResearchService.exec(..))")
	public void addTempList(JoinPoint jp) {

		// HttpServletRequestから、page(SearchPattern)とIPアドレスを取得する。
		HttpServletRequest req = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
		String uri = req.getRequestURI();
		String page = uri.substring(uri.lastIndexOf("/") + 1, uri.length());
		String ip = req.getRemoteAddr();


		GoPokedex goPokedex = ((IndividialValue) jp.getArgs()[0]).getGoPokedex();
		String pokedexId = goPokedex.getPokedexId();

		addTempList(page, pokedexId, ip);
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

		addTempList(page, null, ip);

	}

	/**
	 * 閲覧情報をメモリ上のリストに追加する。
	 *
	 * @param page
	 * @param pokedexId
	 * @param ip
	 */
	public void addTempList(String page, String pokedexId, String ip) {

		ViewTempList viewsTempList = viewsCacheManager.getViewsTempList();
		viewsTempList.add(page, pokedexId, ip);

	}

	/**
	 * ページごとの閲覧数をRedisサーバからすべて取得します。
	 *
	 * @return
	 */
	public Map<String, Integer> findPageViewsAll() {

		Map<String, Integer> rtnMap = viewsCacheManager.findPageViewsAll();
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


}
