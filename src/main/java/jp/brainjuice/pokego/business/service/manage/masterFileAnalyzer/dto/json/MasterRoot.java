package jp.brainjuice.pokego.business.service.manage.masterFileAnalyzer.dto.json;

import java.util.Map;

import lombok.Data;

@Data
public class MasterRoot {

	private String templateId;
	private Map<String, Object> data;
}
