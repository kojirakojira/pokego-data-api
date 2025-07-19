package jp.brainjuice.pokego.business.service.manage.masterFileAnalyzer.dto;

import java.util.Objects;

import jp.brainjuice.pokego.business.service.search.utils.MovesUtils.MoveCategory;
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
public class AdditionalMove {

	private String movementId;
	private MoveCategory category;

	/**
	 * (非 Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		// 衝突を防ぐため素数31を掛ける
		return movementId.hashCode() * 31 + category.name().hashCode();
	}

	/**
	 * (非 Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null || getClass() != obj.getClass()) return false;
		AdditionalMove am = (AdditionalMove) obj;
		return Objects.equals(movementId, am.getMovementId()) && category == am.getCategory();
	}
}
