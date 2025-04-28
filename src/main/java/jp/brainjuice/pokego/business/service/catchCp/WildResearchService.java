package jp.brainjuice.pokego.business.service.catchCp;

import org.springframework.stereotype.Service;

import jp.brainjuice.pokego.business.dao.entity.GoPokedex;
import jp.brainjuice.pokego.business.service.ResearchService;
import jp.brainjuice.pokego.business.service.catchCp.utils.CatchCpUtils;
import jp.brainjuice.pokego.business.service.pokeFilter.dto.SearchValue;
import jp.brainjuice.pokego.business.service.utils.dto.cpIv.IvRangeCp;
import jp.brainjuice.pokego.business.service.utils.dto.cpIv.WildIvRange;
import jp.brainjuice.pokego.web.form.res.catchCp.WildResponse;
import jp.brainjuice.pokego.web.form.res.elem.CatchCp;

@Service
public class WildResearchService implements ResearchService<WildResponse> {

	private CatchCpUtils catchCpUtils;

	public WildResearchService(CatchCpUtils catchCpUtils) {
		this.catchCpUtils = catchCpUtils;
	}

	@Override
	public void exec(SearchValue sv, WildResponse res) {

		GoPokedex goPokedex = sv.getGoPokedex();

		IvRangeCp wild = catchCpUtils.getIvRangeCp(goPokedex, new WildIvRange());
		res.setCatchCp(new CatchCp(wild, null));
	}

}
