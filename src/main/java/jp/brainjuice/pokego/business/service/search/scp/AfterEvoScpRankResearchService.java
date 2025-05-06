package jp.brainjuice.pokego.business.service.search.scp;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import jp.brainjuice.pokego.business.service.search.ResearchService;
import jp.brainjuice.pokego.business.service.search.pokeFilter.dto.SearchValue;
import jp.brainjuice.pokego.business.service.search.pokeFilter.dto.SearchValue.ParamsEnum;
import jp.brainjuice.pokego.business.service.search.utils.PokemonEditUtils;
import jp.brainjuice.pokego.business.service.search.utils.PokemonGoUtils;
import jp.brainjuice.pokego.business.service.search.utils.ScpRankCalculator;
import jp.brainjuice.pokego.business.service.search.utils.dto.AfterEvolIv;
import jp.brainjuice.pokego.business.service.search.utils.evo.EvolutionProvider;
import jp.brainjuice.pokego.dao.jpa.GoPokedexRepository;
import jp.brainjuice.pokego.dao.jpa.entity.Evolution;
import jp.brainjuice.pokego.dao.jpa.entity.GoPokedex;
import jp.brainjuice.pokego.web.search.form.res.MsgLevelEnum;
import jp.brainjuice.pokego.web.search.form.res.elem.ScpRankAllInOne;
import jp.brainjuice.pokego.web.search.form.res.scp.AfterEvoScpRankResponse;

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

	private EvolutionProvider evolutionProvider;

	private ScpRankCalculator scpRankCalculator;

	public AfterEvoScpRankResearchService(
			GoPokedexRepository goPokedexRepository,
			PokemonGoUtils pokemonGoUtils,
			EvolutionProvider evolutionProvider,
			ScpRankCalculator scpRankCalculator) {
		this.goPokedexRepository = goPokedexRepository;
		this.pokemonGoUtils = pokemonGoUtils;
		this.evolutionProvider = evolutionProvider;
		this.scpRankCalculator = scpRankCalculator;
	}

	@Override
	public void exec(SearchValue sv, AfterEvoScpRankResponse res) {

		GoPokedex sp = sv.getGoPokedex();
		Integer spCp = sv.get(ParamsEnum.cp, Integer.class) == null ? null: sv.get(ParamsEnum.cp, Integer.class).intValue(); // 仕様上nullが有り得る
		int iva = sv.get(ParamsEnum.iva, int.class);
		int ivd = sv.get(ParamsEnum.ivd, int.class);
		int ivh = sv.get(ParamsEnum.ivh, int.class);

		res.setSearchPokemon(sp);
		res.setCp(spCp);
		res.setIva(iva);
		res.setIvd(ivd);
		res.setIvh(ivh);

		// 検索されたポケモンのPLを求める。
		String pl = null;
		if (spCp != null) {
			pl = pokemonGoUtils.calcPl(sp, iva, ivd, ivh, spCp);
			pl = pl.replaceAll("^0+", "");
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

		List<Evolution> lineageList = evolutionProvider.getLineageList(sp);
		
		List<String> afEvolPidList = evolutionProvider.getAllAfterEvolution(sp.getPokedexId(), lineageList);
		
		List<GoPokedex> gpAfEvoList = goPokedexRepository.findAllById(afEvolPidList);
		res.setAfEvolIvList(convGpAndScpRankList(gpAfEvoList, iva, ivd, ivh, pl));
		
		// 進化後のポケモン
		List<ScpRankAllInOne> afEvolScpRankList = gpAfEvoList.stream()
				.sorted(PokemonEditUtils.getPokedexComparator(1))
				.map(gp -> createScpRankAllInOne(gp, iva, ivd, ivh))
				.toList();
		res.setAfEvolScpRankList(afEvolScpRankList);
		
		// 検索したポケモンのPvP順位
		res.setTargetGpIv(convGpAndScpRank(sp, iva, ivd, ivh, pl));
	}
	
	private ScpRankAllInOne createScpRankAllInOne(GoPokedex goPokedex, int iva, int ivd, int ivh) {
		return new ScpRankAllInOne(
				goPokedex,
				scpRankCalculator.getSuperLeagueRank(goPokedex, iva, ivd, ivh),
				scpRankCalculator.getHyperLeagueRank(goPokedex, iva, ivd, ivh),
				scpRankCalculator.getMasterLeagueRank(goPokedex, iva, ivd, ivh)
				);
	}

	/**
	 * GoPokedexのリストを、GoPokedexAndCpのリストに変換する。
	 *
	 * @param gpList
	 * @param iva
	 * @param ivd
	 * @param ivh
	 * @return
	 */
	private List<AfterEvolIv> convGpAndScpRankList(List<GoPokedex> gpList, int iva, int ivd, int ivh, String pl) {
		return gpList.stream()
				.map(gp -> convGpAndScpRank(gp, iva, ivd, ivh, pl))
				.collect(Collectors.toList());
	}
	
	/**
	 * GoPokedexを、GoPokedexAndCpのリストに変換する。
	 * @param goPokedex
	 * @param iva
	 * @param ivd
	 * @param ivh
	 * @param pl
	 * @return
	 */
	private AfterEvolIv convGpAndScpRank(GoPokedex goPokedex, int iva, int ivd, int ivh, String pl) {
		AfterEvolIv afterEvolIv = new AfterEvolIv(
				goPokedex,
				StringUtils.isEmpty(pl) ? null : pokemonGoUtils.calcCp(goPokedex, iva, ivd, ivh, pl),
				scpRankCalculator.getSuperLeagueRank(goPokedex, iva, ivd, ivh).getRank(),
				scpRankCalculator.getHyperLeagueRank(goPokedex, iva, ivd, ivh).getRank(),
				scpRankCalculator.getMasterLeagueRank(goPokedex, iva, ivd, ivh).getRank(),
				false,
				false
				);
		
		if (!StringUtils.isEmpty(pl)) {
			if (!scpRankCalculator.SL_CP_LIMIT_PREDICATE.test(afterEvolIv.getCp().intValue())) {
				// スーパーリーグ制限を超えた場合
				afterEvolIv.setSlRank(0);
				afterEvolIv.setSlOver(true);
			}
			if (!scpRankCalculator.HL_CP_LIMIT_PREDICATE.test(afterEvolIv.getCp().intValue())) {
				// ハイパーリーグ制限を超えた場合
				afterEvolIv.setHlRank(0);
				afterEvolIv.setHlOver(true);
			}
		}
		
		return afterEvolIv;
	}

}
