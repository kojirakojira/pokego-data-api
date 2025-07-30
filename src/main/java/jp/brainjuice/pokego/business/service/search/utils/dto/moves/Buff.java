package jp.brainjuice.pokego.business.service.search.utils.dto.moves;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Buff {

	private List<BuffContent> buffList;

	private String buffMsg;

	/** バフ・デバフの発動確率 */
	private double activationChance;
	private String activationChanceStr;
}
