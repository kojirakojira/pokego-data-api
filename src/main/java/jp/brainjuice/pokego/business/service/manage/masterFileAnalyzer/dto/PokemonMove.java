package jp.brainjuice.pokego.business.service.manage.masterFileAnalyzer.dto;

import java.util.Objects;

import jp.brainjuice.pokego.business.constant.LearningPatternEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * ポケモンが覚える技における、普通に覚える技とレガシー技以外の技を保持する。<br>
 * シャドウとかリトレーンとか、あとフォルムチェンジで覚える技とか。
 */
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class PokemonMove {

	private String movementId;
	private LearningPatternEnum category;
	private boolean fromMasterLinkData; // master_link_data.ymlから追加されたかどうか（独自に追加したかどうか）

	/**
	 * (非 Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		int result = 17;
		result = result * 31 + movementId.hashCode();
		result = result * 31 + category.name().hashCode();
		return result;
	}

	/**
	 * (非 Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null || getClass() != obj.getClass()) return false;
		PokemonMove am = (PokemonMove) obj;
		return Objects.equals(movementId, am.getMovementId()) && category == am.getCategory();
	}
}
