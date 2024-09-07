package jp.brainjuice.pokego.business.service.utils.dto.moves;

import jp.brainjuice.pokego.business.service.utils.MovesUtils.BuffTarget1Enum;
import jp.brainjuice.pokego.business.service.utils.MovesUtils.BuffTarget2Enum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class BuffContent {

	private BuffTarget1Enum target1;

	private BuffTarget2Enum target2;

	private int buffEffect;
}
