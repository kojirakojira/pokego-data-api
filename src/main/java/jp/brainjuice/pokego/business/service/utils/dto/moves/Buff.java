package jp.brainjuice.pokego.business.service.utils.dto.moves;

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

	/** バフ・デバフの発動確率 */
	private float activationChance;
}
