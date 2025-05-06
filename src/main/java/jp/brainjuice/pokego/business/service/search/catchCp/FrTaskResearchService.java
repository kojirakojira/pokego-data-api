package jp.brainjuice.pokego.business.service.search.catchCp;

import java.util.Optional;

import org.springframework.stereotype.Service;

import jp.brainjuice.pokego.business.service.search.ResearchService;
import jp.brainjuice.pokego.business.service.search.catchCp.utils.CatchCpUtils;
import jp.brainjuice.pokego.business.service.search.pokeFilter.dto.SearchValue;
import jp.brainjuice.pokego.business.service.search.utils.dto.cpIv.FrTaskIvRange;
import jp.brainjuice.pokego.business.service.search.utils.dto.cpIv.IvRangeCp;
import jp.brainjuice.pokego.dao.jpa.entity.GoPokedex;
import jp.brainjuice.pokego.web.search.form.res.catchCp.FrTaskResponse;
import jp.brainjuice.pokego.web.search.form.res.elem.CatchCp;

@Service
public class FrTaskResearchService implements ResearchService<FrTaskResponse> {

	private CatchCpUtils catchCpUtils;

	public FrTaskResearchService(CatchCpUtils catchCpUtils) {
		this.catchCpUtils = catchCpUtils;
	}

	@Override
	public void exec(SearchValue sv, FrTaskResponse res) {

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

		IvRangeCp frTask = catchCpUtils.getIvRangeCp(goPokedex, new FrTaskIvRange());
		res.setCatchCp(new CatchCp(frTask, null));
	}

}
