package jp.brainjuice.pokego.business.service.search.utils.dto.moves;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class BuffContent {

	public enum BuffTarget1Enum {
		own,
		opp
	}

	public enum BuffTarget2Enum {
		attack,
		defense
	}

	private BuffTarget1Enum target1;

	private BuffTarget2Enum target2;

	private int buffEffect;
}
