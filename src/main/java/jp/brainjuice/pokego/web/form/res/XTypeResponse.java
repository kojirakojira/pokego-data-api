package jp.brainjuice.pokego.web.form.res;

import java.util.LinkedHashSet;
import java.util.List;

import jp.brainjuice.pokego.business.service.utils.dto.XTypeElement;
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
