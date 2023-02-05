package jp.brainjuice.pokego.business.service.utils.dto;

import java.util.HashMap;

import javax.annotation.Nonnull;

import jp.brainjuice.pokego.business.dao.entity.GoPokedex;
import lombok.Data;

@Data
public class IndividialValue {

	@Nonnull
	private GoPokedex goPokedex;
	private Integer iva;
	private Integer ivd;
	private Integer ivh;
	private String pl;
	private ParamsMap paramsMap;

	public IndividialValue(GoPokedex goPokedex, Integer iva, Integer ivd, Integer ivh, String pl) {
		setGoPokedex(goPokedex);
		setIva(iva);
		setIvd(ivd);
		setIvh(ivh);
		setPl(pl);
		setParamsMap(new ParamsMap());
	}

	/**
	 * IndividialValueのフィールドとして定義していない
	 *
	 * @author saibabanagchampa
	 *
	 */
	public enum ParamsEnum {
		/** CP */
		cp,
		/** リーグ */
		league,
		/** 最終進化かどうか */
		finEvo,
	}

	/**
	 * {@link IndividialValue#getParamsMap().get()}のショートカットメソッド
	 *
	 * @param key
	 * @return
	 */
	public Object get(ParamsEnum key) {
		return getParamsMap().get(key);
	}

	public class ParamsMap extends HashMap<String, Object> {

		/**
		 * getメソッドはParamsEnumを指定して呼び出すこと
		 *
		 * @see ParamsEnum
		 * @param key
		 * @return
		 */
		public Object get(ParamsEnum key) {
			return super.get(key.name());
		}

		/**
		 * HashMapのgetの呼び出しは原則禁止。
		 *
		 * @deprecated 引数はParamsEnumを指定してください。
		 */
	    public Object get(Object key) {
	        return super.get(key);
	    }
	}
}
