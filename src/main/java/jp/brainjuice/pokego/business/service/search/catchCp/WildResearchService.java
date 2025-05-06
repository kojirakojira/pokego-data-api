package jp.brainjuice.pokego.business.service.search.catchCp;

import org.springframework.stereotype.Service;

import jp.brainjuice.pokego.business.service.search.ResearchService;
import jp.brainjuice.pokego.business.service.search.catchCp.utils.CatchCpUtils;
import jp.brainjuice.pokego.business.service.search.pokeFilter.dto.SearchValue;
import jp.brainjuice.pokego.business.service.search.utils.dto.cpIv.IvRangeCp;
import jp.brainjuice.pokego.business.service.search.utils.dto.cpIv.WildIvRange;
import jp.brainjuice.pokego.dao.jpa.entity.GoPokedex;
import jp.brainjuice.pokego.web.search.form.res.catchCp.WildResponse;
import jp.brainjuice.pokego.web.search.form.res.elem.CatchCp;

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
