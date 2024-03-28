package jp.brainjuice.pokego.business.service.utils.dto.cpIv;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IvRangeCp {

	private int min;
	private int max;
	private int wbMin;
	private int wbMax;
}
