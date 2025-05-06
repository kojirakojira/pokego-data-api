package jp.brainjuice.pokego.business.service.search.pokeFilter.dto;

import java.lang.reflect.Field;
import java.text.MessageFormat;
import java.util.HashMap;

import jakarta.annotation.Nonnull;

import jp.brainjuice.pokego.business.service.search.ResearchServiceExecutor;
import jp.brainjuice.pokego.business.service.search.pokeFilter.PokemonFilterValue;
import jp.brainjuice.pokego.dao.jpa.entity.GoPokedex;
import jp.brainjuice.pokego.web.search.form.req.ResearchRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * ポケモンの検索条件を保持する。
 *
 * @see ResearchServiceExecutor
 */
@Data
@Slf4j
public class SearchValue {

	@Nonnull
	private GoPokedex goPokedex;

	private PokemonFilterValue filterValue;

	private boolean enableCount;

	/** 検索固有の値 */
	private ParamsMap paramsMap;

	public SearchValue(
			GoPokedex goPokedex,
			ResearchRequest req) {
		setGoPokedex(goPokedex);

		// 絞り込み検索値のセット
		setFilterValue(new PokemonFilterValue(req));

		// Requestの値をParamsMapにセット
		ParamsMap paramsMap = new ParamsMap();
		for (Field field: req.getClass().getDeclaredFields()) {
			field.setAccessible(true);
			try {
				paramsMap.put(field.getName(), field.get(req));
			} catch (IllegalArgumentException | IllegalAccessException e) {
				log.warn(MessageFormat.format(
						"Failed to get field [{1}] of class name [{0}].", req.getClass().getName(), field.getName()));
			}
		}
		setParamsMap(paramsMap);

		setEnableCount(req.isEnableCount());
	}

	/**
	 * 固有の検索値を定義する。
	 *
	 * @author saibabanagchampa
	 *
	 */
	@AllArgsConstructor
	public enum ParamsEnum {
		/** 個体値（こうげき） */
		iva,
		/** 個体値（ぼうぎょ） */
		ivd,
		/** 個体値（hp） */
		ivh,
		/** 個体値（hp） */
		pl,
		/** CP */
		cp,
		/** リーグ */
		league,
		/** 天候ブースト */
		wbFlg,
		/** ポケモンを捕まえるときのシチュエーション */
		situation,
		/** statisticsをレスポンスで返却するかのフラグ */
		statsRequired,
		;

	}

	/**
	 * {@link IndividialValue#getParamsMap().get()}のショートカットメソッド
	 *
	 * @param key
	 * @return
	 */
	public <T> T get(ParamsEnum key, Class<T> clazz) {
		return (T) getParamsMap().get(key, clazz);
	}

	public class ParamsMap extends HashMap<String, Object> {

		/**
		 * getメソッドはParamsEnumを指定して呼び出すこと
		 *
		 * @param key
		 * @return
		 * @see ParamsEnum
		 */
		@SuppressWarnings("unchecked")
		public <T> T get(ParamsEnum key, Class<T> clazz) {
			return (T) super.get(key.name());
		}

		/**
		 * HashMapのgetの呼び出しは原則禁止。取得する場合は、ParamsEnumに定義してください。
		 *
		 * @deprecated 引数はParamsEnumを指定してください。
		 * @see ParamsEnum
		 */
	    public Object get(Object key) {
	        return super.get(key);
	    }
	}
}
