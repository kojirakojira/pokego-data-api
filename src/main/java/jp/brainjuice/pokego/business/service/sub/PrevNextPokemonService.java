package jp.brainjuice.pokego.business.service.sub;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.ibm.icu.text.MessageFormat;

import jp.brainjuice.pokego.business.dao.GoPokedexRepository;
import jp.brainjuice.pokego.business.dao.entity.GoPokedex;
import jp.brainjuice.pokego.web.form.res.MsgLevelEnum;
import jp.brainjuice.pokego.web.form.res.sub.PrevNextPokemonResponse;

@Service
public class PrevNextPokemonService {

	private GoPokedexRepository goPokedexRepository;

	private static final String TARGET_NOT_FOUND_MSG = "存在しない図鑑IDが指定されました。(id: {0})";

	public PrevNextPokemonService(GoPokedexRepository goPokedexRepository) {
		this.goPokedexRepository = goPokedexRepository;
	}

	public void exec(String pokedexId, PrevNextPokemonResponse res) {

		List<GoPokedex> goPokedexList = goPokedexRepository.findAll();

		Optional<GoPokedex> targetGpOp = goPokedexList.stream()
				.filter(gp -> gp.getPokedexId().equals(pokedexId))
				.findFirst();

		if (!targetGpOp.isPresent()) {
			res.setSuccess(false);
			res.setMsgLevel(MsgLevelEnum.error);
			res.setMessage(MessageFormat.format(TARGET_NOT_FOUND_MSG, pokedexId));
		}

		int index = goPokedexList.indexOf(targetGpOp.get());

		if (0 < index) {
			res.setPrev(goPokedexList.get(index - 1));
		}

		if (index < goPokedexList.size() - 1) {
			res.setNext(goPokedexList.get(index + 1));
		}
	}
}
