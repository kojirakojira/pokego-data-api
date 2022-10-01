package jp.brainjuice.pokego.business.service.utils.dto;

import java.util.Map;

import javax.annotation.Nonnull;

import jp.brainjuice.pokego.business.dao.entity.GoPokedex;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class IndividialValue {

	@Nonnull
	private GoPokedex goPokedex;
	private Integer iva;
	private Integer ivd;
	private Integer ivh;
	private String pl;
	private Map<String, Object> paramsMap;
}
