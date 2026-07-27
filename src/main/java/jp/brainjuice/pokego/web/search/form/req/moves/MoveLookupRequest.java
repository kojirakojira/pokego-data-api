package jp.brainjuice.pokego.web.search.form.req.moves;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class MoveLookupRequest {

	private String mid;
	private String name;
}
