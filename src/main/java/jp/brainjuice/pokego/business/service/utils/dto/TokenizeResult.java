package jp.brainjuice.pokego.business.service.utils.dto;

import java.util.List;

import jp.brainjuice.pokego.business.service.utils.memory.PokemonDictionaryInfo;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Kuromojiの形態素解析アルゴリズムの結果を格納する。
 *
 * @author saibabanagchampa
 * @see PokemonDictionaryInfo
 *
 */
@Data
@AllArgsConstructor
public class TokenizeResult {
	/** ユーザ辞書で品詞をpokemonにしたやつ。ポケモンの名称 */
	private List<String> pokemonList;
	/** ユーザ辞書で品詞を名詞にしたやつ。リージョン名や別名 */
	private List<String> otherList;
	/**  ユーザ辞書で品詞をgroupにしたやつ。グループを表す単語。（例：三鳥） */
	private List<String> groupList;
}
