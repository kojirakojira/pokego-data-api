package jp.brainjuice.pokego.web.form.req.others;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class EvoCostRequest {

	@NotNull
	private String costs;
}
