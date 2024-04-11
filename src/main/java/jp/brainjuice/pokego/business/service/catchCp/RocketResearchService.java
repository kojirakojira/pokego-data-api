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
import jp.brainjuice.pokego.business.service.utils.dto.cpIv.RocketIvRange;
import jp.brainjuice.pokego.business.service.utils.dto.cpIv.RocketSakakiIvRange;
import jp.brainjuice.pokego.web.form.res.catchCp.RocketResponse;
import jp.brainjuice.pokego.web.form.res.elem.CatchCp;

/**
 * ロケット団勝利ボーナスで獲得できるポケモンの最低-最高個体値を算出する。
 *
 * @author saibabanagchampa
 *
 */
@Service
public class RocketResearchService implements ResearchService<RocketResponse> {

	private CatchCpUtils catchCpUtils;

	@Autowired
	public RocketResearchService(CatchCpUtils catchCpUtils) {
		this.catchCpUtils = catchCpUtils;
	}

	@Override
	public void exec(SearchValue sv, RocketResponse res) {

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

		IvRangeCp rocket = catchCpUtils.getIvRangeCp(goPokedex, new RocketIvRange());
		IvRangeCp sakaki = catchCpUtils.getIvRangeCp(goPokedex, new RocketSakakiIvRange());
		res.setCatchCp(new CatchCp(rocket, List.of(sakaki)));

	}

}
