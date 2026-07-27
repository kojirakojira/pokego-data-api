package jp.brainjuice.pokego.business.service.manage.masterFileAnalyzer.dto.json;

import lombok.Data;

/**
 * 通常技・スペシャル技兼用（ジム・レイド時）
 */
@Data
public class MoveData {

	private String templateId;
	private Move moveSettings;
}
