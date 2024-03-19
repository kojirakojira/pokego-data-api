package jp.brainjuice.pokego.web.form.req.race;

import java.util.List;

import lombok.Data;

@Data
public class RaceDiffRequest {

	private List<String> pidArr;
	private List<String> nameArr;
}
