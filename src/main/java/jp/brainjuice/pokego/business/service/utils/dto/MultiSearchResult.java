package jp.brainjuice.pokego.business.service.utils.dto;

import java.util.List;

import jp.brainjuice.pokego.web.form.res.MsgLevelEnum;
import lombok.Data;

@Data
public class MultiSearchResult {

	private String message;
	private MsgLevelEnum msgLevel = MsgLevelEnum.info;
	private boolean allUnique;
	private List<PokemonSearchResult> psrArr;
}
