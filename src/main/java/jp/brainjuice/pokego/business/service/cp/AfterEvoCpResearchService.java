package jp.brainjuice.pokego.business.service.cp;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import jp.brainjuice.pokego.business.dao.GoPokedexRepository;
import jp.brainjuice.pokego.business.dao.entity.Evolution;
import jp.brainjuice.pokego.business.dao.entity.GoPokedex;
import jp.brainjuice.pokego.business.service.ResearchService;
import jp.brainjuice.pokego.business.service.pokeFilter.dto.SearchValue;
import jp.brainjuice.pokego.business.service.pokeFilter.dto.SearchValue.ParamsEnum;
import jp.brainjuice.pokego.business.service.utils.PokemonGoUtils;
import jp.brainjuice.pokego.business.service.utils.dto.GoPokedexAndCp;
import jp.brainjuice.pokego.business.service.utils.evo.EvolutionProvider;
import jp.brainjuice.pokego.web.form.res.MsgLevelEnum;
import jp.brainjuice.pokego.web.form.res.cp.AfterEvoCpResponse;

/**
 * 進化後CPを求めます。
 *
 * @author saibabanagchampa
 *
 */
@Service
public class AfterEvoCpResearchService implements ResearchService<AfterEvoCpResponse> {

	private GoPokedexRepository goPokedexRepository;

	private PokemonGoUtils pokemonGoUtils;

	private EvolutionProvider evolutionProvider;

	public AfterEvoCpResearchService(
			GoPokedexRepository goPokedexRepository,
			PokemonGoUtils pokemonGoUtils,
			EvolutionProvider evolutionProvider) {
		this.goPokedexRepository = goPokedexRepository;
		this.pokemonGoUtils = pokemonGoUtils;
		this.evolutionProvider = evolutionProvider;
	}

	@Override
	public void exec(SearchValue sv, AfterEvoCpResponse res) {

		GoPokedex sp = sv.getGoPokedex();
		int spCp = sv.get(ParamsEnum.cp, int.class);
		int iva = sv.get(ParamsEnum.iva, int.class);
		int ivd = sv.get(ParamsEnum.ivd, int.class);
		int ivh = sv.get(ParamsEnum.ivh, int.class);

		res.setSearchPokemon(sp);
		res.setCp(spCp);
		res.setIva(iva);
		res.setIvd(ivd);
		res.setIvh(ivh);

		// 検索されたポケモンのPLを求める。
		String pl = pokemonGoUtils.calcPl(sp, iva, ivd, ivh, spCp);

		if (PokemonGoUtils.DUPLICATE.equals(pl)) {
			res.setMessage("CPに対してPLが複数存在します。");
			res.setMsgLevel(MsgLevelEnum.error);
			return;
		} else if (PokemonGoUtils.NOT_EXIST.equals(pl)) {
			res.setMessage("指定された個体値、CPに対応するPLが存在しません。");
			res.setMsgLevel(MsgLevelEnum.error);
			return;
		}


		res.setPl(pl);
		
		List<Evolution> lineageList = evolutionProvider.getLineageList(sp);
		List<GoPokedex> gpList = goPokedexRepository.findAllById(
				lineageList.stream().map(Evolution::getPokedexId).toList()
				);
		
		List<String> afEvolPidList = evolutionProvider.getAllAfterEvolution(sp.getPokedexId(), lineageList);
		

		// 進化後のポケモン
		List<GoPokedex> afEvolGpList = gpList.stream()
				.filter(gp -> afEvolPidList.contains(gp.getPokedexId()))
				.toList();
		res.setAfEvolCpList(convGpAndCpList(afEvolGpList, iva, ivd, ivh, pl));

		// 進化後のポケモンの別のすがた
		List<GoPokedex> anotherFormGpList = gpList.stream()
				.filter(gp -> !afEvolPidList.contains(gp.getPokedexId()))
				.filter(gp -> !sp.getPokedexId().equals(gp.getPokedexId()))
				.toList();
		res.setAnotherFormList(convGpAndCpList(anotherFormGpList, iva, ivd, ivh, pl));
	}

	/**
	 * GoPokedexのリストを、GoPokedexAndCpのリストに変換します。
	 *
	 * @param pidList
	 * @param iva
	 * @param ivd
	 * @param ivh
	 * @param pl
	 * @return
	 */
	private List<GoPokedexAndCp> convGpAndCpList(List<GoPokedex> pidList, int iva, int ivd, int ivh, String pl) {

		return pidList.stream()
				.map(gp -> {
					int cp = pokemonGoUtils.calcCp(gp, iva, ivd, ivh, pl);
					return new GoPokedexAndCp(gp, cp);
				})
				.collect(Collectors.toList());
	}

}
