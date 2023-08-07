package jp.brainjuice.pokego.web.form.res.elem;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * 汎用的な個体値保持クラス
 *
 * @author saibabanagchampa
 *
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class VersatilityIv {

	/** インクリメントさせた№ */
	private int no;
	private String pl;
	private int iva;
	private int ivd;
	private int ivh;
	private double percent;

}
