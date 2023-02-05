package jp.brainjuice.pokego.web.form.req;

import java.util.List;

import lombok.Data;

@Data
public class RaceDiffRequest {

	private List<String> idArr;
	private List<String> nameArr;
	private boolean finEvo = false;
}
