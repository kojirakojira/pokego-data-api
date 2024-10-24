package jp.brainjuice.pokego.business.service.catchCp;

import java.util.Optional;

import org.springframework.stereotype.Service;

import jp.brainjuice.pokego.business.dao.GoPokedexRepository;
import jp.brainjuice.pokego.business.dao.entity.GoPokedex;
import jp.brainjuice.pokego.business.service.ResearchService;
import jp.brainjuice.pokego.business.service.catchCp.utils.CatchCpUtils;
import jp.brainjuice.pokego.business.service.pokeFilter.dto.SearchValue;
import jp.brainjuice.pokego.business.service.utils.dto.cpIv.EggsIvRange;
import jp.brainjuice.pokego.business.service.utils.dto.cpIv.IvRangeCp;
import jp.brainjuice.pokego.business.service.utils.evo.EvolutionProvider;
import jp.brainjuice.pokego.web.form.res.MsgLevelEnum;
import jp.brainjuice.pokego.web.form.res.catchCp.EggsResponse;
import jp.brainjuice.pokego.web.form.res.elem.CatchCp;

@Service
public class EggsResearchService implements ResearchService<EggsResponse> {

	private CatchCpUtils catchCpUtils;

	private EvolutionProvider evolutionProvider;

	private GoPokedexRepository goPokedexRepository;

	private String BEF_EVO_MSG = "進化前のポケモンで算出しました。";

	public EggsResearchService(
			CatchCpUtils catchCpUtils,
			EvolutionProvider evolutionProvider,
			GoPokedexRepository goPokedexRepository) {
		this.catchCpUtils = catchCpUtils;
		this.evolutionProvider = evolutionProvider;
		this.goPokedexRepository = goPokedexRepository;
	}

	@Override
	public void exec(SearchValue sv, EggsResponse res) {

		// リクエストから受け取った値でGoPokedexを初期化
		GoPokedex goPokedex = sv.getGoPokedex();

		// メガシンカ後の場合はメガシンカ前を取得する。
		Optional<GoPokedex> befMegaGp = catchCpUtils.getGoPokedexForMega(goPokedex, res);
		goPokedex = befMegaGp.orElse(goPokedex);

		res.setBefore(true); // 進化なし、メガシンカありのポケモンの考慮（メガディアンシー）
		res.setBefGp(goPokedex);

		// 進化前が存在する場合は、進化前のgoPokedexに置き換える。
		// ガーメイルの場合、一番最初のミノムッチで置き換える。
		String pid = goPokedex.getPokedexId();
		String befPid = evolutionProvider.getRoot(pid).get(0);
		if (!pid.equals(befPid)) {
			goPokedex = goPokedexRepository.findById(befPid).get();
			res.setBefGp(goPokedex);

			res.setBefore(true);
			res.setMessage(BEF_EVO_MSG);
			res.setMsgLevel(MsgLevelEnum.warn);
		}

		IvRangeCp eggs = catchCpUtils.getIvRangeCp(goPokedex, new EggsIvRange());
		res.setCatchCp(new CatchCp(eggs, null));
	}

}
