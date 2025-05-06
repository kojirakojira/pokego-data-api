package jp.brainjuice.pokego.web.search.form.res.type;

import java.util.LinkedHashSet;
import java.util.List;

import jp.brainjuice.pokego.business.service.search.utils.dto.XTypeElement;
import jp.brainjuice.pokego.web.search.form.res.Response;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class XTypeResponse extends Response {

	private String own1;
	private String own2;
	private String opp1;
	private String opp2;
	private String emphasis;
	private List<XTypeElement> typeRankList;
	private LinkedHashSet<String> typeComments;
}
