package jp.brainjuice.pokego.business.service.search.moves;

import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import jp.brainjuice.pokego.business.constant.AttackAnnotationTypeEnum;
import jp.brainjuice.pokego.business.service.search.ResearchService;
import jp.brainjuice.pokego.business.service.search.pokeFilter.dto.SearchValue;
import jp.brainjuice.pokego.business.service.search.utils.MovesUtils;
import jp.brainjuice.pokego.business.service.search.utils.dto.moves.DispPokemonChargedAttack;
import jp.brainjuice.pokego.business.service.search.utils.dto.moves.DispPokemonFastAttack;
import jp.brainjuice.pokego.dao.jpa.GoPokedexRepository;
import jp.brainjuice.pokego.dao.jpa.PokemonChargedAttackRepository;
import jp.brainjuice.pokego.dao.jpa.PokemonFastAttackRepository;
import jp.brainjuice.pokego.dao.jpa.entity.AttackAdditionalInfo;
import jp.brainjuice.pokego.dao.jpa.entity.GoPokedex;
import jp.brainjuice.pokego.dao.jpa.entity.PokemonChargedAttack;
import jp.brainjuice.pokego.dao.jpa.entity.PokemonFastAttack;
import jp.brainjuice.pokego.utils.BjUtils;
import jp.brainjuice.pokego.web.search.form.res.moves.PokemonAttackResponse;

@Service
public class PokemonAttackResearchService implements ResearchService<PokemonAttackResponse> {

	private PokemonFastAttackRepository pokemonFastAttackRepository;

	private PokemonChargedAttackRepository pokemonChargedAttackRepository;

	private MovesUtils movesUtils;

	private GoPokedexRepository goPokedexRepository;

	public PokemonAttackResearchService(
			PokemonFastAttackRepository pokemonFastAttackRepository,
			PokemonChargedAttackRepository pokemonChargedAttackRepository,
			MovesUtils movesUtils,
			GoPokedexRepository goPokedexRepository) {
		this.pokemonFastAttackRepository = pokemonFastAttackRepository;
		this.pokemonChargedAttackRepository = pokemonChargedAttackRepository;

		this.movesUtils = movesUtils;
		this.goPokedexRepository = goPokedexRepository;
	}

	@Override
	public void exec(SearchValue sv, PokemonAttackResponse res) {

		GoPokedex gp = sv.getGoPokedex();
		res.setGoPokedex(gp);

		if (!StringUtils.isEmpty(gp.getPreMegaPokedexId())) {
			// メガシンカの場合
			GoPokedex preMegaGp = goPokedexRepository.findById(gp.getPreMegaPokedexId()).orElseThrow();
			res.setPreMegaGp(preMegaGp);
			return;
		}

		{
			// 通常技
			List<PokemonFastAttack> pokemonFastAttackList = pokemonFastAttackRepository.findByPokedexIdJoinFastAttack(gp.getPokedexId());

			List<DispPokemonFastAttack> dispPokemonFastAttackList = pokemonFastAttackList.stream()
					.map(pfa -> {
						DispPokemonFastAttack dpfa = new DispPokemonFastAttack(movesUtils.convDispFastAttack(pfa.getFastAttack()));
						dpfa.setLearningPattern(pfa.getLearningPattern());
						dpfa.setLearningPatternName(pfa.getLearningPattern().getJpn());
						return dpfa;
					})
					.sorted((o1, o2) -> BjUtils.getCollator().compare(o1.getName(), o2.getName()))
					.toList();

			int i = 1;
			for (DispPokemonFastAttack dpfa: dispPokemonFastAttackList) {
				dpfa.setNo(i++);
			}
			res.setFastAttackList(dispPokemonFastAttackList);
		}

		{
			// スペシャル技
			List<PokemonChargedAttack> pokemonChargedAttackList = pokemonChargedAttackRepository.findByPokedexIdJoinChargedAttack(gp.getPokedexId());

			List<DispPokemonChargedAttack> dispPokemonFastAttackList = pokemonChargedAttackList.stream()
					.map(pca -> {
						DispPokemonChargedAttack dpca = new DispPokemonChargedAttack(movesUtils.convDispChargedAttack(pca.getChargedAttack()));
						dpca.setLearningPattern(pca.getLearningPattern());
						dpca.setLearningPatternName(pca.getLearningPattern().getJpn());
						// 覚え方の注釈
						Optional<String> leaningPatternAnnosOp = pca.getAttackAdditionalInfo().stream()
								.filter(aai -> aai.getAnnotationType() == AttackAnnotationTypeEnum.learning_pattern)
								.map(AttackAdditionalInfo::getText)
								.findFirst();
						dpca.setLearningPatternAnnos(leaningPatternAnnosOp.orElse(""));
						return dpca;
					})
					.sorted((o1, o2) -> BjUtils.getCollator().compare(o1.getName(), o2.getName()))
					.toList();
			int i = 1;
			for (DispPokemonChargedAttack dpca: dispPokemonFastAttackList) {
				dpca.setNo(i++);
			}
			res.setChargedAttackList(dispPokemonFastAttackList);
		}
	}
}
