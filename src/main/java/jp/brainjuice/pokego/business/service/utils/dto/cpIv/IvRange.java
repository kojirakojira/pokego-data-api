package jp.brainjuice.pokego.business.service.utils.dto.cpIv;

import jp.brainjuice.pokego.business.service.cp.CpIvResearchService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 個体値の振れ幅を表現する。
 *
 * @author saibabanagchampa
 * @see CpIvResearchService
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class IvRange {

	// CpMultiplierMapでPLをStringで扱っているため、型はStringで扱う。
	// 天候ブーストが存在しない場合は、nullを設定する。

	/** 最低PL */
	private String minPl;
	/** 最低PL(天候ブーストあり) */
	private String minPlWb;
	/** 最高PL */
	private String maxPl;
	/** 最高PL(天候ブーストあり) */
	private String maxPlWb;

	/** 最低個体値 */
	private int minIv;
	/** 最低個体値（天候ブーストあり） */
	private Integer minIvWb;
	/** 最高個体値 */
	private int maxIv;
	/** 最高個体値（天候ブーストあり） */
	private Integer maxIvWb;
}
