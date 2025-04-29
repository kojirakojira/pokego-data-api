package jp.brainjuice.pokego.business.service.utils.dto;

import jp.brainjuice.pokego.business.dao.entity.GoPokedex;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AfterEvolIv {

	private GoPokedex goPokedex;
	private Integer cp;
	private int slRank;
	private int hlRank;
	private int mlRank;
	private boolean slOver; // スーパーリーグ制限を超えている場合
	private boolean hlOver; // スーパーリーグ制限を超えている場合
}
