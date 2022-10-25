package jp.brainjuice.pokego.web.form.req.research;

/**
 * ResearchSearviceExecutorを使用してポケモン情報の検索をする場合に継承するインタフェースです。
 *
 * @author saibabanagchampa
 *
 */
public interface ResearchRequest {

	public String getId();
	public void setId(String id);

	public String getName();
	public void setName(String name);

	public Integer getIva();
	public void setIva(Integer iva);

	public Integer getIvd();
	public void setIvd(Integer ivd);

	public Integer getIvh();
	public void setIvh(Integer ivh);

	public String getPl();
	public void setPl(String pl);

}
