package jp.brainjuice.pokego.web.form.res.elem;

import jp.brainjuice.pokego.business.service.utils.ScpRankCalculator.LeagueEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ScpRank {

	private LeagueEnum league;
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

	/**
	 * 小数点第二位で四捨五入してセット。
	 *
	 * @param sp
	 */
	public void setSp(double sp) {
		this.sp = Math.round(sp * 100.0) / 100.0;
	}

	/**
	 * 小数点第二位で四捨五入してセット。
	 *
	 * @param scp
	 */
	public void setScp(double scp) {
		this.scp = Math.round(scp * 100.0) / 100.0;
	}

	/**
	 * 小数点第二位で四捨五入してセット。
	 *
	 * @param percent
	 */
	public void setPercent(double percent) {
		this.percent = Math.round(percent * 100.0) / 100.0;
	}
}
