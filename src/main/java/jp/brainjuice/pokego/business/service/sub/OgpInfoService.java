package jp.brainjuice.pokego.business.service.sub;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import jp.brainjuice.pokego.business.constant.Type.TypeEnum;
import jp.brainjuice.pokego.business.dao.GoPokedexRepository;
import jp.brainjuice.pokego.business.dao.entity.GoPokedex;
import jp.brainjuice.pokego.business.service.utils.PokemonEditUtils;
import jp.brainjuice.pokego.business.service.utils.dto.type.TwoTypeKey;
import jp.brainjuice.pokego.utils.BjUtils;
import jp.brainjuice.pokego.web.form.res.sub.OgpPokemonResponse;
import jp.brainjuice.pokego.web.form.res.sub.OgpTypeResponse;

@Service
public class OgpInfoService {

	private GoPokedexRepository goPokedexRepository;

	public OgpInfoService(GoPokedexRepository goPokedexRepository) {
		this.goPokedexRepository = goPokedexRepository;
	}

	/**
	 * OGPで使用する情報を取得する。（ポケモン）
	 *
	 * @param pokedexId
	 * @param res
	 */
	public void execPokemonInfo(String pokedexId, OgpPokemonResponse res) {

		if (StringUtils.isEmpty(pokedexId)) {
			res.setName("");
			res.setImage("");
			return;
		}

		Optional<GoPokedex> goPokedexOp = goPokedexRepository.findById(pokedexId);

		String name = "";
		String image = "";
		if (goPokedexOp.isPresent()) {
			name = PokemonEditUtils.appendRemarks(goPokedexOp.get());
			image = StringUtils.isEmpty(image) ? "" : goPokedexOp.get().getImage2();
		}

		res.setName(name);
		res.setImage(image);

	}

	/**
	 * OGPで使用する情報を取得する。（タイプ）
	 *
	 * @param type1
	 * @param type2
	 * @param ogpTypeResponse
	 */
	public void execTypeInfo(String type1, String type2, OgpTypeResponse ogpTypeResponse) {

		Optional<String> type1Op = Optional.of(type1);
		Optional<String> type2Op = Optional.of(type2);

		if (!(type1Op.isPresent() || TypeEnum.isDefined(type1))
				&& (type2Op.isPresent() || TypeEnum.isDefined(type2))) {
			ogpTypeResponse.setType("");
		}

		TwoTypeKey ttk = new TwoTypeKey(
				(type1Op.isPresent() ? TypeEnum.valueOf(type1Op.get()) : null),
				(type2Op.isPresent() ? TypeEnum.valueOf(type2Op.get()) : null));

		ogpTypeResponse.setType(ttk.toJpnString());
	}

	public void execTypeInfo(String pid, OgpTypeResponse ogpTypeResponse) {


		Optional<GoPokedex> goPokedexOp = goPokedexRepository.findById(pid);

		if (!goPokedexOp.isPresent()) {
			ogpTypeResponse.setType("");
		}

		GoPokedex gp = goPokedexOp.get();
		String type1 = BjUtils.replaceEmpty(gp.getType1());
		String type2 = BjUtils.replaceEmpty(gp.getType2());
		ogpTypeResponse.setType(type1 + (type2.isEmpty() ? "" : ", " + type2));
	}
}
