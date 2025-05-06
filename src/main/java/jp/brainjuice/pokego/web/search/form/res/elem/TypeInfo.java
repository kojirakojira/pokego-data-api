package jp.brainjuice.pokego.web.search.form.res.elem;

import jp.brainjuice.pokego.business.constant.Type.TypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TypeInfo {

	private TypeEnum type;
	private String jpn;
	private Color color;
}
