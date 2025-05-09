package jp.brainjuice.pokego.web.search.form.res.others;

import java.util.List;
import java.util.Map;

import jp.brainjuice.pokego.web.search.form.res.elem.TypeInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * クライアント定数をまとめて取得する用のオブジェクト
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Constants {

	private List<TypeInfo> typeList;
	private Map<String, String> regionMap;
	private Map<String, String> genMap;
	private Map<String, String> filterItemMap;
	private List<String> plList;
	private Map<String, String> situationMap;
}
