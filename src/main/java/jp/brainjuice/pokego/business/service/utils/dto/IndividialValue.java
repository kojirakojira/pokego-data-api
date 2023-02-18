package jp.brainjuice.pokego.business.service.utils.dto;

import java.lang.reflect.Field;
import java.util.HashMap;

import javax.annotation.Nonnull;

import com.ibm.icu.text.MessageFormat;

import jp.brainjuice.pokego.business.dao.entity.GoPokedex;
import jp.brainjuice.pokego.business.service.utils.IndividialValueUtils;
import jp.brainjuice.pokego.web.form.req.research.ResearchRequest;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class IndividialValue {

	@Nonnull
	private GoPokedex goPokedex;

	private PokemonFilterValue filterValue;

	/** 検索固有の値 */
	private ParamsMap paramsMap;

	public IndividialValue(
			GoPokedex goPokedex,
			ResearchRequest req) {
		setGoPokedex(goPokedex);

		// 絞り込み検索値のセット
		setFilterValue(IndividialValueUtils.createPokemonFilterValue(req));

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
		iva(Integer.class),
		/** 個体値（ぼうぎょ） */
		ivd(Integer.class),
		/** 個体値（hp） */
		ivh(Integer.class),
		/** 個体値（hp） */
		pl(Integer.class),
		/** CP */
		cp(Integer.class),
		/** リーグ */
		league(String.class),
		;

		@Getter(value = AccessLevel.PUBLIC)
		private Class<?> dataType;
	}

	/**
	 * {@link IndividialValue#getParamsMap().get()}のショートカットメソッド
	 *
	 * @param key
	 * @return
	 */
	public Object get(ParamsEnum key) {
		return key.getDataType().cast(getParamsMap().get(key));
	}

	public class ParamsMap extends HashMap<String, Object> {

		/**
		 * getメソッドはParamsEnumを指定して呼び出すこと
		 *
		 * @param key
		 * @return
		 * @see ParamsEnum
		 */
		public Object get(ParamsEnum key) {
			return key.getDataType().cast(super.get(key.name()));
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
