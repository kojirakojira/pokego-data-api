package jp.brainjuice.pokego.web.form.res;

import java.util.LinkedHashSet;
import java.util.List;

import jp.brainjuice.pokego.business.service.utils.dto.IroiroTypeRankElement;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class IroiroTypeRankResponse extends Response {

	private String searchPattern;
	private List<IroiroTypeRankElement> typeRankList;
	private LinkedHashSet<String> typeComments;
	private String msgsHeader;
	private boolean MsgDecoration;
}
