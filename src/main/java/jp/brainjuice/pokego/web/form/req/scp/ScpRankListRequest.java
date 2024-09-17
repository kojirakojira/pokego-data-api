package jp.brainjuice.pokego.web.form.req.scp;

import jakarta.validation.constraints.NotNull;
import jp.brainjuice.pokego.web.form.req.ResearchRequestImpl;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper=false)
@AllArgsConstructor
@NoArgsConstructor
public class ScpRankListRequest extends ResearchRequestImpl {

	@NotNull
	private String league;
}
