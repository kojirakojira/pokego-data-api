package jp.brainjuice.pokego.web.form.res.pl;

import java.util.ArrayList;

import jp.brainjuice.pokego.web.form.res.ResearchResponse;
import jp.brainjuice.pokego.web.form.res.elem.PlCp;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper=false)
@NoArgsConstructor
public class PlListResponse extends ResearchResponse {

	private int iva;
	private int ivd;
	private int ivh;
	private ArrayList<PlCp> plList;
}
