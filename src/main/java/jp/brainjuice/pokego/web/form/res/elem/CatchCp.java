package jp.brainjuice.pokego.web.form.res.elem;

import java.util.List;

import jp.brainjuice.pokego.business.service.utils.dto.cpIv.IvRangeCp;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CatchCp {

	private IvRangeCp normal;

	private List<IvRangeCp> others;

}
