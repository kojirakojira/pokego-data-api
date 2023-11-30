package jp.brainjuice.pokego.web.form.req.cp;

import jp.brainjuice.pokego.web.form.req.ResearchRequestImpl;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class CpIvRequest extends ResearchRequestImpl {

	private String situation;
	private int cp;
	private boolean wbFlg;
}