package jp.brainjuice.pokego.web.form.res.elem;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ScpRank {

	private int rank;
	private int iva;
	private int ivd;
	private int ivh;
	private int cp;
	/** ステ積 */
	private double sp;
	private double scp;
	private String pl;
	private double percent;

	public ScpRank(int iva, int ivd, int ivh) {
		setIva(iva);
		setIvd(ivd);
		setIvh(ivh);
	}
}
