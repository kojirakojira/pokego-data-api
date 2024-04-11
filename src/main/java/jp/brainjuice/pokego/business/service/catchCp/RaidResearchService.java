package jp.brainjuice.pokego.business.service.catchCp;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jp.brainjuice.pokego.business.dao.entity.GoPokedex;
import jp.brainjuice.pokego.business.service.ResearchService;
import jp.brainjuice.pokego.business.service.catchCp.utils.CatchCpUtils;
import jp.brainjuice.pokego.business.service.utils.dto.SearchValue;
import jp.brainjuice.pokego.business.service.utils.dto.cpIv.IvRangeCp;
import jp.brainjuice.pokego.business.service.utils.dto.cpIv.RaidIvRange;
import jp.brainjuice.pokego.business.service.utils.dto.cpIv.RaidShadowIvRange;
import jp.brainjuice.pokego.web.form.res.catchCp.RaidResponse;
import jp.brainjuice.pokego.web.form.res.elem.CatchCp;

@Service
public class RaidResearchService implements ResearchService<RaidResponse> {

	private CatchCpUtils catchCpUtils;

	@Autowired
	public RaidResearchService(CatchCpUtils catchCpUtils) {
		this.catchCpUtils = catchCpUtils;
	}

	@Override
	public void exec(SearchValue sv, RaidResponse res) {

		GoPokedex goPokedex = sv.getGoPokedex();

		{
			// メガシンカ後のポケモンの場合は、メガシンカ前のポケモンを取得する。
			Optional<GoPokedex> befMegaGp = catchCpUtils.getGoPokedexForMega(goPokedex, res);
			if (befMegaGp.isPresent()) {
				// nullでなかったらgoPokedexはメガシンカ後。メガシンカ前のポケモンで後続処理を進める。
				goPokedex = befMegaGp.get();
				res.setMega(true);
				res.setBefMegaGp(befMegaGp.get());
			}
		}

		IvRangeCp raid = catchCpUtils.getIvRangeCp(goPokedex, new RaidIvRange());
		IvRangeCp shadowRaid = catchCpUtils.getIvRangeCp(goPokedex, new RaidShadowIvRange());
		res.setCatchCp(new CatchCp(raid, List.of(shadowRaid)));
	}

}
