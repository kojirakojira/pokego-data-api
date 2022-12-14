package jp.brainjuice.pokego.business.service.research;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ibm.icu.text.MessageFormat;

import jp.brainjuice.pokego.business.dao.GoPokedexRepository;
import jp.brainjuice.pokego.business.dao.entity.GoPokedex;
import jp.brainjuice.pokego.business.service.PokemonSearchService;
import jp.brainjuice.pokego.business.service.utils.dto.IndividialValue;
import jp.brainjuice.pokego.business.service.utils.dto.PokemonSearchResult;
import jp.brainjuice.pokego.filter.log.LogUtils;
import jp.brainjuice.pokego.utils.exception.BadRequestException;
import jp.brainjuice.pokego.web.form.req.Request;
import jp.brainjuice.pokego.web.form.res.Response;

@Component
public class ResearchServiceExecutor<T extends Response> {

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
	public void execute(Request req, T res, ResearchService<T> researchService) throws BadRequestException {

		if (req.getId() != null) {
			Optional<GoPokedex> goPokedexOp = goPokedexRepository.findById(req.getId());

			if (!goPokedexOp.isPresent()) {
				res.setSuccess(false);
				res.setMessage("存在しないIDです。");
				return;
			}

			research(goPokedexOp.get(), req, res, researchService);

		} else if (req.getName() != null) {
			PokemonSearchResult psr = pokemonSearchService.search(req.getName());
			res.setPokemonSearchResult(psr);

			if (psr.isUnique()) {
				research(psr.getGoPokedex(), req, res, researchService);

			}
		} else {
			throw new BadRequestException();
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
	private void research(GoPokedex goPokedex, Request req, T res, ResearchService<T> researchService) {


		// 個体値をセットする。（GoPokedexだけ必須。）
		IndividialValue iv = new IndividialValue(
				goPokedex,
				req.getIva(),
				req.getIvd(),
				req.getIvh(),
				req.getPl(),
				new HashMap<String, Object>());

		// Requestの値をivのマップにセット
		for (Field field: req.getClass().getDeclaredFields()) {
			field.setAccessible(true);
			try {
				iv.getParamsMap().put(field.getName(), field.get(req));
			} catch (IllegalArgumentException | IllegalAccessException e) {
				LogUtils.getLog(this).warn(MessageFormat.format(
						"Failed to get field [{1}] of class name [{0}].", req.getClass().getName(), field.getName()));
			}
		}

		// 実行
		researchService.exec(iv, res);

		// 図鑑№、ポケモンのセット
		res.setPokedexId(goPokedex.getPokedexId());
		res.setName(goPokedex.getName());

		res.setSuccess(true);
	}
}
