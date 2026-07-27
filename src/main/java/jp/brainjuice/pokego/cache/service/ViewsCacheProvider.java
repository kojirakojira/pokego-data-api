package jp.brainjuice.pokego.cache.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import jp.brainjuice.pokego.business.service.search.pokeFilter.dto.SearchValue;
import jp.brainjuice.pokego.business.service.search.utils.PokemonEditUtils;
import jp.brainjuice.pokego.cache.inmemory.topic.data.PageNameEnum;
import jp.brainjuice.pokego.dao.jpa.entity.GoPokedex;

/**
 * 閲覧情報にアクセスするためのプロバイダクラスです。
 * 閲覧情報は直接Redisサーバに送信されます。
 *
 * 閲覧情報を処理する目的は大きく2つあります。
 * 1つ目は、Topic機能で直近のアクセス上位を集計することです。
 * 2つ目は、ページ、ポケモン単位でトータルの閲覧数を集計することです。
 *
 * @author saibabanagchampa
 *
 */
@Service
@Aspect
public class ViewsCacheProvider {

	private ViewsCacheManager viewsCacheManager;

	public ViewsCacheProvider(ViewsCacheManager viewsCacheManager) {
		this.viewsCacheManager = viewsCacheManager;
	}

	/**
	 * 閲覧情報をRedisに追加する。<br>
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
	 * 閲覧情報をRedisに追加する。<br>
	 * 単一のポケモンに限定できないパターンの場合の呼び出し口
	 *
	 */
	public void addTempList() {
		HttpServletRequest req = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
				.getRequest();
		String uri = req.getRequestURI();
		String page = uri.substring(uri.lastIndexOf("/") + 1, uri.length());
		String ip = req.getRemoteAddr();

		addTempList(PageNameEnum.valueOf(page), (String) null, ip);
	}

	/**
	 * 閲覧情報をRedisに追加する。<br>
	 * 単体のポケモンに対する閲覧の場合の呼び出し口。
	 *
	 */
	public void addTempList(String pokedexId) {
		HttpServletRequest req = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
				.getRequest();
		String uri = req.getRequestURI();
		String page = uri.substring(uri.lastIndexOf("/") + 1, uri.length());
		String ip = req.getRemoteAddr();

		addTempList(PageNameEnum.valueOf(page), pokedexId, ip);
	}

	/**
	 * 閲覧情報をRedisに追加する。<br>
	 * 複数ポケモンに対する閲覧の場合の呼び出し口。
	 *
	 */
	public void addTempList(List<String> pokedexIds) {
		HttpServletRequest req = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
				.getRequest();
		String uri = req.getRequestURI();
		String page = uri.substring(uri.lastIndexOf("/") + 1, uri.length());
		String ip = req.getRemoteAddr();

		addTempList(PageNameEnum.valueOf(page), pokedexIds, ip);
	}

	/**
	 * 閲覧情報をRedisに追加する。
	 *
	 * @param page
	 * @param pokedexId
	 * @param ip
	 */
	public void addTempList(PageNameEnum page, String pokedexId, String ip) {
		addTempList(page, pokedexId == null ? null : Collections.singletonList(pokedexId), ip);
	}

	/**
	 * 閲覧情報をRedisに追加する。（複数ポケモン用）
	 * 処理の詳細は ViewsCacheManager 側に委譲します。
	 *
	 * @param page
	 * @param pokedexIds
	 * @param ip
	 */
	public void addTempList(PageNameEnum page, List<String> pokedexIds, String ip) {
		viewsCacheManager.addView(page, pokedexIds, ip);
	}
}
