package jp.brainjuice.pokego.business.service.search.moves;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import jp.brainjuice.pokego.business.service.search.ResearchService;
import jp.brainjuice.pokego.business.service.search.pokeFilter.dto.SearchValue;
import jp.brainjuice.pokego.business.service.search.pokeFilter.dto.SearchValue.ParamsEnum;
import jp.brainjuice.pokego.business.service.search.utils.GymRaidDamageCalculator;
import jp.brainjuice.pokego.business.service.search.utils.MovesUtils;
import jp.brainjuice.pokego.business.service.search.utils.dto.damage.GymRaidAttackScoreInDto;
import jp.brainjuice.pokego.business.service.search.utils.dto.damage.GymRaidAttackScoreOutDto;
import jp.brainjuice.pokego.business.service.search.utils.dto.moves.MoveCombination;
import jp.brainjuice.pokego.dao.jpa.GoPokedexRepository;
import jp.brainjuice.pokego.dao.jpa.PokemonChargedAttackRepository;
import jp.brainjuice.pokego.dao.jpa.PokemonFastAttackRepository;
import jp.brainjuice.pokego.dao.jpa.entity.ChargedAttack;
import jp.brainjuice.pokego.dao.jpa.entity.FastAttack;
import jp.brainjuice.pokego.dao.jpa.entity.GoPokedex;
import jp.brainjuice.pokego.dao.jpa.entity.PokemonChargedAttack;
import jp.brainjuice.pokego.dao.jpa.entity.PokemonFastAttack;
import jp.brainjuice.pokego.web.search.form.res.moves.GymRaidPokeMoveCombiResponse;

@Service
public class GymRaidPokeMoveCombiResearchService implements ResearchService<GymRaidPokeMoveCombiResponse> {

	private PokemonFastAttackRepository pokemonFastAttackRepository;

	private PokemonChargedAttackRepository pokemonChargedAttackRepository;

	private GoPokedexRepository goPokedexRepository;

	private GymRaidDamageCalculator gymRaidDamageCalculator;

	private MovesUtils movesUtils;

	public GymRaidPokeMoveCombiResearchService(
			PokemonFastAttackRepository pokemonFastAttackRepository,
			PokemonChargedAttackRepository pokemonChargedAttackRepository,
			GoPokedexRepository goPokedexRepository,
			GymRaidDamageCalculator gymRaidDamageCalculator,
			MovesUtils movesUtils) {
		this.pokemonFastAttackRepository = pokemonFastAttackRepository;
		this.pokemonChargedAttackRepository = pokemonChargedAttackRepository;
		this.goPokedexRepository = goPokedexRepository;
		this.gymRaidDamageCalculator = gymRaidDamageCalculator;
		this.movesUtils = movesUtils;
	}

	@Override
	public void exec(SearchValue sv, GymRaidPokeMoveCombiResponse res) {

		GoPokedex gp = sv.getGoPokedex();
		res.setGoPokedex(gp);
		long limit = sv.get(ParamsEnum.limit, long.class);

		if (!gp.isImplFlg()) {
			// 未実装のポケモンの場合、技は表示しない
			res.setMoveCombiList(List.of());
			return;
		}

		GoPokedex targetGp = gp;
		if (!StringUtils.isEmpty(gp.getPreMegaPokedexId())) {
			// メガシンカの場合
			GoPokedex preMegaGp = goPokedexRepository.findById(gp.getPreMegaPokedexId()).orElseThrow();
			res.setPreMegaGp(preMegaGp);

			targetGp = preMegaGp;
		}

		String pokedexId = targetGp.getPokedexId();
		List<PokemonFastAttack> pokemonFastAttackList = movesUtils.convHiddenPowerForPokemonFastAttackList( // めざめるパワーを変換
				pokemonFastAttackRepository.findByPokedexIdJoinFastAttack(pokedexId)
				).stream()
				.filter(pfa -> !MovesUtils.TRANSFORM_MOVE_ID.equals(pfa.getMoveId())) // へんしんを排除する。
				.toList();
		List<PokemonChargedAttack> pokemonChargedAttackList = pokemonChargedAttackRepository.findByPokedexIdJoinChargedAttack(pokedexId);


		List<MoveCombination> combiList = new ArrayList<>(pokemonFastAttackList.size() * pokemonChargedAttackList.size());
		GymRaidAttackScoreInDto inDto = new GymRaidAttackScoreInDto(); // 処理効率化のためインスタンスを使い回す
		inDto.setGoPokedex(gp); // メガシンカ後の場合、技の検索は進化前で行うが、攻撃スコアの算出はメガシンカ後で行う
		for (PokemonFastAttack pfa : pokemonFastAttackList) {
			for (PokemonChargedAttack pca : pokemonChargedAttackList) {
				MoveCombination combi = new MoveCombination();
				FastAttack fa = pfa.getFastAttack();
				ChargedAttack ca = pca.getChargedAttack();

				combi.setFaMoveId(fa.getMoveId());
				combi.setFaName(fa.getName());
				combi.setFaType(fa.getType());
				combi.setCaMoveId(ca.getMoveId());
				combi.setCaName(ca.getName());
				combi.setCaType(ca.getType());

				inDto.setFastAttack(fa);
				inDto.setChargedAttack(ca);
				GymRaidAttackScoreOutDto outDto = gymRaidDamageCalculator.attackScore(inDto);
				combi.setFaAttackScore(outDto.getFastAttackScore());
				combi.setCaAttackScore(outDto.getChargedAttackScore());
				combi.setAttackScore(outDto.getAttackScore());

				combiList.add(combi);
			}
		}

		Stream<MoveCombination> combiStream = combiList.stream()
				.sorted((o1, o2) -> Double.compare(o2.getAttackScore(), o1.getAttackScore()));
		if (limit != -1L) {
			combiStream = combiStream.limit(limit);
		}
		combiList = combiStream.toList();

		res.setMoveCombiList(combiList);
	}

}
