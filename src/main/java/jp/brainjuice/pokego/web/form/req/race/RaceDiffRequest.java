package jp.brainjuice.pokego.web.form.req.race;

import java.util.List;

import jp.brainjuice.pokego.web.form.res.elem.PidAndName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RaceDiffRequest {

	private List<PidAndName> pidAndNameArr;
}
