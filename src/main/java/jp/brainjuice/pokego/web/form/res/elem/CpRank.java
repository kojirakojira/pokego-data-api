package jp.brainjuice.pokego.web.form.res.elem;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CpRank {

	private int rank;
	private int iva;
	private int ivd;
	private int ivh;
	private int cp;
	private double percent;

	public CpRank(int iva, int ivd, int ivh) {
		setIva(iva);
		setIvd(ivd);
		setIvh(ivh);
	}
}
