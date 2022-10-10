package jp.brainjuice.pokego.cache.inmemory;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.ListIterator;

import org.springframework.stereotype.Component;

import jp.brainjuice.pokego.cache.dao.entity.PokemonViewInfo;
import jp.brainjuice.pokego.filter.log.LogUtils;
import jp.brainjuice.pokego.utils.BjUtils;


@Component
public class TempTopicPokemonList extends ArrayList<PokemonViewInfo> {

	private static int COUNT_PERIOD = 3;

	/**
	 * 集計用の話題のポケモンリストに追加する
	 *
	 * @param pokedexId
	 * @param ip
	 */
	public void addTmpList(String pokedexId, String ip) {

		try {
			// 5分前の時間
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.MINUTE, -5);
			Date before = cal.getTime();

			// 5分前よりも未来に同じIPアドレスが存在しているかを確認する(連打防止)
			boolean flg = false;
			for (ListIterator<PokemonViewInfo> ite = listIterator(size()); ite.hasPrevious();) {
				PokemonViewInfo viewInfo = ite.previous();

				// 5分前より過去になったらループ中断
				if (before.after(viewInfo.getTime())) {
					break;
				}

				if (ip.equals(viewInfo.getIp()) && pokedexId.equals(viewInfo.getPokedexId())) {
					// IPと閲覧したイベントが一致したらフラグを立ててループ中断
					flg = true;
					break;
				}
			}

			if (!flg) {
				// viewsListに追加
				add(new PokemonViewInfo(pokedexId, ip, BjUtils.now()));
			}

		} catch (Exception e) {
			// 例外が発生した場合、無視する
			LogUtils.getLog(this).error("Processing failure.", e);
		}
	}

	/**
	 * 集計用の話題のイベントリストの集計対象外の要素を削除する。
	 */
	public void removeTmpTopicEventList() {

		// 集計の開始時間を求める
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.HOUR_OF_DAY, COUNT_PERIOD * -1);
		Date before = cal.getTime();

		boolean flg = false;
		int idx = 0;
		int size = size();
		// リストは時間順に並んでいる
		for (int i = size - 1; i >= 0; i--) {
			if (before.after(get(i).getTime())) {
				idx = i;
				flg = true;
				break;
			}
		}

		if (flg) {
			ArrayList<PokemonViewInfo> tempTopicEventList = new ArrayList<PokemonViewInfo>(subList(idx + 1, size));
			addAll(tempTopicEventList);
		}
	}
}
