package jp.brainjuice.pokego.utils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.stereotype.Component;

import jp.brainjuice.pokego.dao.redis.LastUpdateRepository;
import jp.brainjuice.pokego.dao.redis.entity.LastUpdate;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class LastUpdateService {

	private LastUpdateRepository lastUpdateRepository;

	LastUpdateService (LastUpdateRepository lastUpdateRepository) {
		this.lastUpdateRepository = lastUpdateRepository;
	}
	/**
	 * 最終更新日のキー名
	 *
	 * @author saibabanagchampa
	 */
	public enum Keys {
		unimplPokemon, // 未実装ポケモン一覧
		dynamaxImplPokemon //ダイマックス、キョダイマックス実装済み一覧
	}

	public String getYmd(Keys key) {
		return lastUpdateRepository.findById(key.name()).orElseThrow().getYmd();
	}

	/**
	 * 文字列をフォーマットに従ってparseし、登録する。
	 * @param
	 * @return format {@link #getFormat()}
	 */
	public String saveYmd(String ymdStr) {
		List<LastUpdate> lastUpdateList = Arrays.stream(ymdStr.split("\\n"))
				.map(row -> {
					String[] strArr = row.split(":");
					Keys key = Keys.valueOf(strArr[0]); // 一度enumに変換して不正なキーが入らないことを担保する
					String ymd = strArr[1];
					return new LastUpdate(key.name(), ymd);
				})
				.toList();
		// Redisを更新
		lastUpdateRepository.saveAll(lastUpdateList);

		return getFormat();
	}

	/**
	 * saveYmdメソッドで登録するためのフォーマットを取得する
	 *
	 * @return format Keys:yyyy/MM/dd\nKeys:yyyy/MM/dd...
	 */
	public String getFormat() {
		Map<String, String> map = StreamSupport.stream(lastUpdateRepository.findAll().spliterator(), false)
				.collect(Collectors.toMap(LastUpdate::getKey, LastUpdate::getYmd));

		return Arrays.stream(Keys.values())
				.map(keys -> {
					String ymd = "yyyy/MM/dd";
					if (map.containsKey(keys.name())) {
						ymd = map.get(keys.name());
					}
					return keys.name() + ":" + ymd;
				})
				.collect(Collectors.joining("\n"));
	}
}
