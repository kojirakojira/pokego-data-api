package jp.brainjuice.pokego.business.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jp.brainjuice.pokego.business.dao.GoPokedexRepository;
import jp.brainjuice.pokego.business.dao.entity.GoPokedex;
import jp.brainjuice.pokego.business.service.general.PokemonSearchService;
import jp.brainjuice.pokego.business.service.utils.dto.PokemonSearchResult;
import jp.brainjuice.pokego.business.service.utils.dto.SearchValue;
import jp.brainjuice.pokego.utils.exception.BadRequestException;
import jp.brainjuice.pokego.web.form.req.ResearchRequest;
import jp.brainjuice.pokego.web.form.res.MsgLevelEnum;
import jp.brainjuice.pokego.web.form.res.ResearchResponse;

@Component
public class ResearchServiceExecutor<T extends ResearchResponse> {

	private GoPokedexRepository goPokedexRepository;

	private PokemonSearchService pokemonSearchService;

	@Autowired
	public ResearchServiceExecutor(
			GoPokedexRepository goPokedexRepository,
			PokemonSearchService pokemonSearchService) {
		this.goPokedexRepository = goPokedexRepository;
		this.pokemonSearchService = pokemonSearchService;
	}

	/**
	 * ResearchServiceを実行します。<br><br>
	 *
	 * Requestにidとnameどちらが設定されているかにより、処理を振り分けます。<br>
	 * どちらも設定されている場合はidが優先されます。<br>
	 * <table>
	 * <tr>
	 * <td>Requestに図鑑№(id)が設定されている場合は、<br>
	 * 図鑑№からGoPokedexを検索し、ResearchServiceを実行します。</td>
	 * </tr>
	 * <tr>
	 * <td>Requestにポケモン(name)が設定されている場合は、<br>
	 * nameからGoPokedexの検索をします。1件ヒットした場合のみ、<br>
	 * ResearchServiceを実行します。</td>
	 * </tr>
	 * </table>
	 *
	 * @param req
	 * @param res
	 * @param researchService
	 * @throws BadRequestException
	 */
	public void execute(ResearchRequest req, T res, ResearchService<T> researchService) throws BadRequestException {

		if (req.getPid() != null) {
			Optional<GoPokedex> goPokedexOp = goPokedexRepository.findById(req.getPid());

			if (!goPokedexOp.isPresent()) {
				res.setSuccess(true);
				res.setMessage("存在しないIDです。");
				res.setMsgLevel(MsgLevelEnum.error);
				return;
			}

			research(goPokedexOp.get(), req, res, researchService);

		} else if (req.getName() != null) {
			PokemonSearchResult psr = pokemonSearchService.search(req.getName());
			res.setPokemonSearchResult(psr);

			if (psr.isUnique()) {
				research(psr.getGoPokedex(), req, res, researchService);
			}

			res.setSuccess(true);
		} else {
			throw new BadRequestException(req.getPid(), req.getName());
		}
	}

	/**
	 * ResearchServiceを実行します。
	 *
	 * @param goPokedex
	 * @param req
	 * @param res
	 * @param researchService
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 */
	private void research(GoPokedex goPokedex, ResearchRequest req, T res, ResearchService<T> researchService) {


		// 個体値をセットする。（GoPokedexだけ必須。）
		SearchValue sv = new SearchValue(goPokedex, req);

		// 実行
		researchService.exec(sv, res);

		// 図鑑№、ポケモン、備考のセット
		res.setPokedexId(goPokedex.getPokedexId());
		res.setName(goPokedex.getName());
		res.setImage(goPokedex.getImage());
		res.setRemarks(goPokedex.getRemarks());

		res.setSuccess(true);
	}
}
