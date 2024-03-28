package jp.brainjuice.pokego.web.form.res.elem;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CatchCp {

	private int min;
	private int max;
	private int wbMin;
	private int wbMax;

	private String otherName;
	private int otherMin;
	private int otherMax;
}
