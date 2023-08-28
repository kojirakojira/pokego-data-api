package jp.brainjuice.pokego.business.service.cp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jp.brainjuice.pokego.business.dao.GoPokedexRepository;
import jp.brainjuice.pokego.business.dao.entity.GoPokedex;
import jp.brainjuice.pokego.business.service.ResearchService;
import jp.brainjuice.pokego.business.service.utils.PokemonEditUtils;
import jp.brainjuice.pokego.business.service.utils.PokemonGoUtils;
import jp.brainjuice.pokego.business.service.utils.dto.GoPokedexAndCp;
import jp.brainjuice.pokego.business.service.utils.dto.SearchValue;
import jp.brainjuice.pokego.business.service.utils.dto.SearchValue.ParamsEnum;
import jp.brainjuice.pokego.business.service.utils.memory.EvolutionInfo;
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

	private EvolutionInfo evolutionInfo;

	@Autowired
	public AfterEvoCpResearchService(
			GoPokedexRepository goPokedexRepository,
			PokemonGoUtils pokemonGoUtils,
			EvolutionInfo evolutionInfo) {
		this.goPokedexRepository = goPokedexRepository;
		this.pokemonGoUtils = pokemonGoUtils;
		this.evolutionInfo = evolutionInfo;
	}

	@Override
	public void exec(SearchValue sv, AfterEvoCpResponse res) {

		GoPokedex sp = sv.getGoPokedex();
		int spCp = ((Integer) sv.get(ParamsEnum.cp)).intValue();
		int iva = ((Integer) sv.get(ParamsEnum.iva)).intValue();
		int ivd = ((Integer) sv.get(ParamsEnum.ivd)).intValue();
		int ivh = ((Integer) sv.get(ParamsEnum.ivh)).intValue();

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

		// 進化後のポケモン
		List<String> afEvoPidList = getAllAfterEvoPidList(sp);
		List<GoPokedex> gpAfEvoList = goPokedexRepository.findAllById(afEvoPidList);
		res.setAfEvoList(convGpAndCpList(gpAfEvoList, iva, ivd, ivh, pl));

		// 進化後のポケモンの別のすがた
		List<GoPokedex> gpAnotherFormList = goPokedexRepository.findAllById(getAnotherFormPidList(sp, afEvoPidList));
		res.setAnotherFormList(convGpAndCpList(gpAnotherFormList, iva, ivd, ivh, pl));
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

	/**
	 * 別のすがたをすべて取得する。
	 *
	 * @param goPokedex
	 * @param pidList
	 * @return
	 */
	private List<String> getAnotherFormPidList(GoPokedex goPokedex, List<String> pidList) {

		return Stream.concat(Stream.of(goPokedex.getPokedexId()), pidList.stream())
				.flatMap(pid -> evolutionInfo.getAnotherFormList(pid).stream())
				.sorted(PokemonEditUtils.getPokedexIdComparator())
				.collect(Collectors.toList());
	}

}
