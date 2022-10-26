package jp.brainjuice.pokego.cache.inmemory;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.ListIterator;

import javax.annotation.concurrent.ThreadSafe;

import org.springframework.stereotype.Component;

import com.ibm.icu.text.MessageFormat;

import jp.brainjuice.pokego.utils.BjUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * 閲覧情報を一時的にメモリで保持するクラス<br>
 * ※Iteratorを使用する場合はsynchronizedさせる必要があります。<br>
 * ※連打抑止は、APサーバ単位ごとに機能します。
 *
 * @author saibabanagchampa
 *
 */
@Component
@ThreadSafe
@Slf4j
public class ViewTempList extends ArrayList<ViewTempInfo> {

	private static final String VIEW_ADD_START_LOG = "Start add ViewTempList. Page(SearchPattern): {0}, PokedexId: {1}, IP: {2}";
	private static final String VIEW_ADD_END_LOG = "End add ViewTempList. ({0})";
	private static final String VIEW_ADD_END_LOG_ADDED = "Added!!";
	private static final String VIEW_ADD_END_LOG_NOT_ADDED = "Not added.";

	private static final String VIEW_REMOVE_START_LOG = "> Start remove ViewTempList.";
	private static final String VIEW_REMOVE_END_LOG = "> End remove ViewTempList. ({0})";
	private static final String VIEW_REMOVE_END_LOG_REMOVED = "removed!!";
	private static final String VIEW_REMOVE_END_LOG_NOT_REMOVED = "Not removed.";

	private static final String RTN_LIST_LOG = "> > rtnList: {0}";
	private static final String VIEW_TEMP_LIST_LOG = "> > ViewTempList: {0}";

	/** 連打抑止の期間 */
	private static final int MINUTE_5 = 5;

	/**
	 * コンストラクタ<br>
	 * SynchronizedRandomAccessListとして扱う。
	 */
	public ViewTempList() {
		super(Collections.synchronizedList(new ArrayList<ViewTempInfo>()));
	}

	/**
	 * 過去5分以内に同じアクセスがない場合、ViewTempInfoを後ろに追加する。
	 *
	 * @param page
	 * @param pokedexId
	 * @param ip
	 */
	public synchronized void add(String page, String pokedexId, String ip) {

		log.info(MessageFormat.format(VIEW_ADD_START_LOG, page, pokedexId, ip));

		// 5分前の時間を取得
		Date before = beforeTime(MINUTE_5);

		// 5分前よりも未来に同じアクセス（同じページ、図鑑№、IP）が存在しているかを確認する(連打防止)
		boolean existsFlg = false;
		for (ListIterator<ViewTempInfo> ite = listIterator(size()); ite.hasPrevious();) {
			ViewTempInfo vti = ite.previous();

			// 5分前より過去になったらループ中断
			if (before.after(vti.getTime())) {
				existsFlg = true;
				break;
			}

			// ページ、図鑑№、IPアドレスが一致している場合
			// ※図鑑Noがnullの場合は照合せずに、図鑑Noは一致しているものとする。
			if (vti.getPage().equals(page)
					&& (vti.getPokedexId() == null || pokedexId == null || vti.getPokedexId().equals(pokedexId))
					&& vti.getIp().equals(ip)) {
				existsFlg = true;
				break;
			}
		}

		if (!existsFlg) {
			// 過去5分以内に対象のユーザが同じページを閲覧していない場合、追加する。
			add(new ViewTempInfo(page, pokedexId, ip, BjUtils.now()));
		}

		log.info(MessageFormat.format(VIEW_ADD_END_LOG, existsFlg ? VIEW_ADD_END_LOG_NOT_ADDED : VIEW_ADD_END_LOG_ADDED));
	}

	/**
	 * 集計対象の閲覧情報リストをディープコピーで取得する。<br>
	 * ※取得した閲覧情報はViewTempListから削除する。<br>
	 * スレッドセーフ（多分）<br>
	 * 5分前より昔の閲覧情報を集計対象とする。（5分前だと連打抑止に影響するため。）
	 *
	 * @return
	 */
	public ArrayList<ViewTempInfo> getAggregateTargetList() {

		log.info(VIEW_REMOVE_START_LOG);

		// 返却用のリスト（集計対象の閲覧情報のリスト）
		ArrayList<ViewTempInfo> rtnList;

		// 5分前の時間を取得
		Date before = beforeTime(MINUTE_5);

		boolean flg = false;
		int idx = 0;
		int size = size();
		// リストは時間順に並んでいる
		for (int i = size - 1; i >= 0; i--) {
			if (before.after(get(i).getTime())) {
				idx = i + 1;
				flg = true;
				break;
			}
		}

		if (flg) {
			// 5分前より古い閲覧情報を返却用リストに追加
			rtnList = new ArrayList<ViewTempInfo>(subList(0, idx));
			// 5分前より古い閲覧情報を削除
			subList(0, idx).clear();

		} else {
			// 集計対象の閲覧情報が存在しない場合、空のリストを返却する。
			rtnList = new ArrayList<ViewTempInfo>();
		}

		log.info(MessageFormat.format(RTN_LIST_LOG, rtnList.toString()));
		log.info(MessageFormat.format(VIEW_TEMP_LIST_LOG, toString()));
		log.info(MessageFormat.format(
				VIEW_REMOVE_END_LOG, flg ? VIEW_REMOVE_END_LOG_REMOVED : VIEW_REMOVE_END_LOG_NOT_REMOVED));

		return rtnList;
	}

	/**
	 * n時間前の時間を取得する。
	 *
	 * @param minute
	 * @return
	 */
	private Date beforeTime(int minute) {

		Calendar cal = Calendar.getInstance();
		cal.setTime(BjUtils.now());
		cal.add(Calendar.MINUTE, -minute);
		return cal.getTime();
	}
}
