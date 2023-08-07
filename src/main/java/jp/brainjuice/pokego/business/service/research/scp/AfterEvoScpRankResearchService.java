package jp.brainjuice.pokego.business.service.research.scp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jp.brainjuice.pokego.business.dao.GoPokedexRepository;
import jp.brainjuice.pokego.business.dao.entity.GoPokedex;
import jp.brainjuice.pokego.business.service.research.ResearchService;
import jp.brainjuice.pokego.business.service.utils.PokemonEditUtils;
import jp.brainjuice.pokego.business.service.utils.PokemonGoUtils;
import jp.brainjuice.pokego.business.service.utils.ScpRankCalculator;
import jp.brainjuice.pokego.business.service.utils.dto.AfterEvoIv;
import jp.brainjuice.pokego.business.service.utils.dto.SearchValue;
import jp.brainjuice.pokego.business.service.utils.dto.SearchValue.ParamsEnum;
import jp.brainjuice.pokego.business.service.utils.memory.EvolutionInfo;
import jp.brainjuice.pokego.web.form.res.MsgLevelEnum;
import jp.brainjuice.pokego.web.form.res.research.scp.AfterEvoScpRankResponse;

/**
 * 進化後PvP順位を求めます。
 *
 * @author saibabanagchampa
 *
 */
@Service
public class AfterEvoScpRankResearchService implements ResearchService<AfterEvoScpRankResponse> {

	private GoPokedexRepository goPokedexRepository;

	private PokemonGoUtils pokemonGoUtils;

	private EvolutionInfo evolutionInfo;

	private ScpRankCalculator scpRankCalculator;

	@Autowired
	public AfterEvoScpRankResearchService(
			GoPokedexRepository goPokedexRepository,
			PokemonGoUtils pokemonGoUtils,
			EvolutionInfo evolutionInfo,
			ScpRankCalculator scpRankCalculator) {
		this.goPokedexRepository = goPokedexRepository;
		this.pokemonGoUtils = pokemonGoUtils;
		this.evolutionInfo = evolutionInfo;
		this.scpRankCalculator = scpRankCalculator;
	}

	@Override
	public void exec(SearchValue sv, AfterEvoScpRankResponse res) {

		GoPokedex sp = sv.getGoPokedex();
		Integer spCp = sv.get(ParamsEnum.cp) == null ? null: (Integer) sv.get(ParamsEnum.cp); // 仕様上nullが有り得る
		int iva = ((Integer) sv.get(ParamsEnum.iva)).intValue();
		int ivd = ((Integer) sv.get(ParamsEnum.ivd)).intValue();
		int ivh = ((Integer) sv.get(ParamsEnum.ivh)).intValue();

		res.setSearchPokemon(sp);
		res.setCp(spCp);
		res.setIva(iva);
		res.setIvd(ivd);
		res.setIvh(ivh);

		// 検索されたポケモンのPLを求める。
		String pl = null;
		if (spCp != null) {
			pl = pokemonGoUtils.calcPl(sp, iva, ivd, ivh, spCp);
			res.setPl(pl);

			if (PokemonGoUtils.DUPLICATE.equals(pl)) {
				res.setMessage("CPに対してPLが複数存在します。");
				res.setMsgLevel(MsgLevelEnum.error);
				return;
			} else if (PokemonGoUtils.NOT_EXIST.equals(pl)) {
				res.setMessage("指定された個体値、CPに対応するPLが存在しません。");
				res.setMsgLevel(MsgLevelEnum.error);
				return;
			}
		}

		List<GoPokedex> gpAfEvoList = goPokedexRepository.findAllById(getAllAfterEvoPidList(sp));
		res.setAfEvoList(convGpAndScpRankList(gpAfEvoList, iva, ivd, ivh, pl));
	}

	/**
	 * GoPokedexのリストを、GoPokedexAndCpのリストに変換します。
	 *
	 * @param pidList
	 * @param iva
	 * @param ivd
	 * @param ivh
	 * @return
	 */
	private List<AfterEvoIv> convGpAndScpRankList(List<GoPokedex> pidList, int iva, int ivd, int ivh, String pl) {

		return pidList.stream()
				.map(gp -> new AfterEvoIv(
						gp,
						pl == null ? null : pokemonGoUtils.calcCp(gp, iva, ivd, ivh, pl),
						scpRankCalculator.getSuperLeagueRank(gp, iva, ivd, ivh).getRank(),
						scpRankCalculator.getHyperLeagueRank(gp, iva, ivd, ivh).getRank(),
						scpRankCalculator.getMasterLeagueRank(gp, iva, ivd, ivh).getRank()))
				.collect(Collectors.toList());
	}

	/**
	 * 進化後のPokedexIdをすべて取得する。<br>
	 * （第一形態のポケモンで検索された場合、第三形態のポケモンのPokedexIdも取得する。）
	 *
	 * @param searchPokemon
	 * @return
	 */
	private List<String> getAllAfterEvoPidList(GoPokedex searchPokemon) {

		List<String> afEvoList = new ArrayList<>();
		{
			List<String> searchPidList = Arrays.asList(searchPokemon.getPokedexId());

			while (true) {
				List<String> hieList = searchPidList.stream()
						.flatMap(pid -> evolutionInfo.getAfterEvolution(pid).stream())
						.collect(Collectors.toList());

				if (hieList.isEmpty()) break;

				afEvoList.addAll(hieList);
				searchPidList = hieList;
			}
		}

		afEvoList.sort(PokemonEditUtils.getPokedexIdComparator());

		return afEvoList;
	}

}
