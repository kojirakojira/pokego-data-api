package jp.brainjuice.pokego.business.service.search.moves;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import jp.brainjuice.pokego.business.constant.AttackAnnotationTypeEnum;
import jp.brainjuice.pokego.business.service.search.utils.MovesUtils;
import jp.brainjuice.pokego.business.service.search.utils.MovesUtils.MoveCode;
import jp.brainjuice.pokego.business.service.search.utils.PokemonGoUtils;
import jp.brainjuice.pokego.business.service.search.utils.dto.moves.ChargedAttackDetails;
import jp.brainjuice.pokego.business.service.search.utils.dto.moves.ChargedAttackRank;
import jp.brainjuice.pokego.business.service.search.utils.dto.moves.DispChargedAttack;
import jp.brainjuice.pokego.business.service.search.utils.dto.moves.DispFastAttack;
import jp.brainjuice.pokego.business.service.search.utils.dto.moves.FastAttackDetails;
import jp.brainjuice.pokego.business.service.search.utils.dto.moves.FastAttackRank;
import jp.brainjuice.pokego.business.service.search.utils.dto.moves.GoPokedexAndMoveInfo;
import jp.brainjuice.pokego.business.service.search.utils.dto.moves.MoveSearchResult;
import jp.brainjuice.pokego.dao.jpa.ChargedAttackRepository;
import jp.brainjuice.pokego.dao.jpa.FastAttackRepository;
import jp.brainjuice.pokego.dao.jpa.PokemonChargedAttackRepository;
import jp.brainjuice.pokego.dao.jpa.PokemonFastAttackRepository;
import jp.brainjuice.pokego.dao.jpa.entity.Attack;
import jp.brainjuice.pokego.dao.jpa.entity.AttackAdditionalInfo;
import jp.brainjuice.pokego.dao.jpa.entity.ChargedAttack;
import jp.brainjuice.pokego.dao.jpa.entity.FastAttack;
import jp.brainjuice.pokego.dao.jpa.entity.PokemonChargedAttack;
import jp.brainjuice.pokego.dao.jpa.entity.PokemonFastAttack;
import jp.brainjuice.pokego.utils.exception.BadRequestException;
import jp.brainjuice.pokego.web.search.form.req.moves.MoveLookupRequest;
import jp.brainjuice.pokego.web.search.form.res.MsgLevelEnum;
import jp.brainjuice.pokego.web.search.form.res.moves.MoveLookupResponse;

@Service
public class MoveLookupService {

	private MoveSearchService moveSearchService;

	private MovesUtils movesUtils;

	private FastAttackRepository fastAttackRepository;

	private ChargedAttackRepository chargedAttackRepository;

	private PokemonFastAttackRepository pokemonFastAttackRepository;

	private PokemonChargedAttackRepository pokemonChargedAttackRepository;

	private PokemonGoUtils pokemonGoUtils;

	public MoveLookupService(MoveSearchService moveSearchService,
			MovesUtils movesUtils,
			FastAttackRepository fastAttackRepository,
			ChargedAttackRepository chargedAttackRepository,
			PokemonFastAttackRepository pokemonFastAttackRepository,
			PokemonChargedAttackRepository pokemonChargedAttackRepository,
			PokemonGoUtils pokemonGoUtils) {
		this.moveSearchService = moveSearchService;
		this.movesUtils = movesUtils;
		this.fastAttackRepository = fastAttackRepository;
		this.chargedAttackRepository = chargedAttackRepository;
		this.pokemonFastAttackRepository = pokemonFastAttackRepository;
		this.pokemonChargedAttackRepository = pokemonChargedAttackRepository;
		this.pokemonGoUtils = pokemonGoUtils;
	}

	public boolean check(MoveLookupRequest req, MoveLookupResponse res) {

		String moveId = req.getMid();

		if (!StringUtils.isEmpty(moveId)) {
			Optional<MoveCode> moveCode = movesUtils.getMoveCode(moveId);
			if (!moveCode.isPresent()) {
				// moveIdの形式が正しくない場合
				res.setSuccess(false);
				res.setMessage("IDが不正です。");
				res.setMsgLevel(MsgLevelEnum.error);
				return false;
			}
		}

		return true;
	}

	public void execute(MoveLookupRequest req, MoveLookupResponse res) throws BadRequestException {

		if (!StringUtils.isEmpty(req.getMid())) {
			// moveIdから取得
			String moveId = req.getMid();
			MoveCode moveCode = movesUtils.getMoveCode(moveId).orElseThrow();
			switch (moveCode) {
			case fast_attack:
				executeFastAttack(moveId, res);
				break;
			case charged_attack:
				executeChargedAttack(moveId, res);
				break;
			default:
				break;
			}

		} else if (!StringUtils.isEmpty(req.getName())) {
			// nameから取得
			MoveSearchResult moveSearchResult = moveSearchService.search(req.getName());
			res.setMoveSearchResult(moveSearchResult);

			if (moveSearchResult.isUnique()) {
				String moveId = res.getMoveSearchResult().getSimpMove().getMoveId();
				MoveCode moveCode = movesUtils.getMoveCode(moveId).orElseThrow();
				switch (moveCode) {
				case fast_attack:
					executeFastAttack(moveId, res);
					break;
				case charged_attack:
					executeChargedAttack(moveId, res);
					break;
				default:
					break;
				}
			}
		} else {
			throw new BadRequestException(req.getMid(), req.getName());
		}

	}

	/**
	 * FastAttackの情報を生成しセットする。
	 *
	 * @param moveId
	 * @param res
	 */
	private void executeFastAttack(String moveId, MoveLookupResponse res) {

		// 変換後のめざめるパワーの場合は、元に戻す
		String mid = movesUtils.resetHiddenPowerMoveId(moveId);

		List<FastAttack> fastAttackList = fastAttackRepository.findAll();
		Optional<FastAttack> fastAttackOp = fastAttackList.stream()
				.filter(fa -> mid.equals(fa.getMoveId()))
				.findFirst();

		if (!fastAttackOp.isPresent()) {
			res.setSuccess(false);
			res.setMsgLevel(MsgLevelEnum.error);
			res.setMessage("存在しない技IDが指定されました。");
			return;
		}

		FastAttack fastAttack = fastAttackOp.get();

		res.setMoveId(fastAttack.getMoveId());
		res.setName(fastAttack.getName());
		res.setFastAttackDetails(createFastAttackDetails(fastAttack.getMoveId(), fastAttack, fastAttackList));
	}

	private FastAttackDetails createFastAttackDetails(String moveId, FastAttack fastAttack, List<FastAttack> fastAttackList) {
		FastAttackDetails details = new FastAttackDetails();

		details.setFastAttack(movesUtils.convDispFastAttack(fastAttack));

		details.setFastAttackRank(createFastAttackRank(moveId, fastAttackList));

		List<PokemonFastAttack> pokemonFastAttackList = pokemonFastAttackRepository.findByMoveIdJoinGoPokedex(moveId);

		// 覚えるポケモン
		AtomicInteger counter = new AtomicInteger();
		List<GoPokedexAndMoveInfo> learnPokemonList = pokemonFastAttackList.stream()
				.filter(pfa -> pfa.getGoPokedex().isImplFlg()) // 実装済みのポケモンに絞り込む
				.map(pfa -> {
					GoPokedexAndMoveInfo gpami = new GoPokedexAndMoveInfo();
					gpami.setNo(counter.incrementAndGet());
					gpami.setGoPokedex(pfa.getGoPokedex());
					gpami.setCp(pokemonGoUtils.calcMaxBaseCp(pfa.getGoPokedex()));
					gpami.setLearningPattern(pfa.getLearningPattern());
					gpami.setLearningPatternName(pfa.getLearningPattern().getJpn());
					Optional<String> annosOp = pfa.getAttackAdditionalInfo().stream()
							.filter(aai -> aai.getAnnotationType() == AttackAnnotationTypeEnum.learning_pattern)
							.map(AttackAdditionalInfo::getText)
							.findAny();
					gpami.setLearningPatternAnnos(annosOp.orElse(""));
					return gpami;
				})
				.toList();
		details.setLearnPokemonList(learnPokemonList);

		List<DispFastAttack> sameTypeMoveList = movesUtils.convDispFastAttackList(
				fastAttackList.stream()
				.filter(fa -> fa.getType() == fastAttack.getType())
				.toList());
		details.setSameTypeMoveList(sameTypeMoveList);

		return details;
	}

	private FastAttackRank createFastAttackRank(String moveId, List<FastAttack> fastAttackList) {

		FastAttackRank fastAttackRank = new FastAttackRank();
		/* ジム・レイド */
		// ダメージ
		fastAttackRank.setGymPowerRank(getRankDouble(moveId, FastAttack::getGymPower, fastAttackList, true));
		// ゲージ増加量
		fastAttackRank.setGymEnergyIncrAmountRank(getRankInt(moveId, FastAttack::getGymEnergyIncrAmount, fastAttackList, true));
		// DPS
		fastAttackRank.setDpsRank(getRankDouble(moveId, FastAttack::getDps, fastAttackList, true));
		// EPS
		fastAttackRank.setEpsRank(getRankDouble(moveId, FastAttack::getEps, fastAttackList, true));
		// 技の発生時間
		fastAttackRank.setDamageSecondsRank(getRankInt(moveId, FastAttack::getDamageMs, fastAttackList, false));
		/* PvP */
		// ダメージ
		fastAttackRank.setPvpPowerRank(getRankDouble(moveId, FastAttack::getPvpPower, fastAttackList, true));
		// ゲージ増加量
		fastAttackRank.setPvpEnergyIncrAmountRank(getRankInt(moveId, FastAttack::getPvpEnergyIncrAmount, fastAttackList, true));
		// DPT
		fastAttackRank.setDptRank(getRankDouble(moveId, FastAttack::getDpt, fastAttackList, true));
		// EPT
		fastAttackRank.setEptRank(getRankDouble(moveId, FastAttack::getEpt, fastAttackList, true));

		fastAttackRank.setTotalCount(fastAttackList.size());

		return fastAttackRank;
	}

	/**
	 * ChargedAttackの情報を生成しセットする。
	 *
	 * @param moveId
	 * @param res
	 */
	private void executeChargedAttack(String moveId, MoveLookupResponse res) {

		List<ChargedAttack> chargedAttackList = chargedAttackRepository.findAll();
		Optional<ChargedAttack> chargedAttackOp = chargedAttackList.stream()
				.filter(fa -> moveId.equals(fa.getMoveId()))
				.findFirst();

		if (!chargedAttackOp.isPresent()) {
			res.setSuccess(false);
			res.setMsgLevel(MsgLevelEnum.error);
			res.setMessage("存在しない技IDが指定されました。");
			return;
		}

		ChargedAttack chargedAttack = chargedAttackOp.get();

		res.setMoveId(chargedAttack.getMoveId());
		res.setName(chargedAttack.getName());
		res.setChargedAttackDetails(createChargedAttackDetails(moveId, chargedAttack, chargedAttackList));
	}

	private ChargedAttackDetails createChargedAttackDetails(String moveId, ChargedAttack chargedAttack, List<ChargedAttack> chargedAttackList) {
		ChargedAttackDetails details = new ChargedAttackDetails();

		details.setChargedAttack(movesUtils.convDispChargedAttack(chargedAttack));

		details.setChargedAttackRank(createChargedAttackRank(moveId, chargedAttackList));

		List<PokemonChargedAttack> pokemonChargedAttackList = pokemonChargedAttackRepository.findByMoveIdJoinGoPokedex(moveId);

		// 覚えるポケモン
		AtomicInteger counter = new AtomicInteger();
		List<GoPokedexAndMoveInfo> learnPokemonList = pokemonChargedAttackList.stream()
				.filter(pfa -> pfa.getGoPokedex().isImplFlg()) // 実装済みのポケモンに絞り込む
				.map(pca -> {
					GoPokedexAndMoveInfo gpami = new GoPokedexAndMoveInfo();
					gpami.setNo(counter.incrementAndGet());
					gpami.setGoPokedex(pca.getGoPokedex());
					gpami.setCp(pokemonGoUtils.calcMaxBaseCp(pca.getGoPokedex()));
					gpami.setLearningPattern(pca.getLearningPattern());
					gpami.setLearningPatternName(pca.getLearningPattern().getJpn());
					Optional<String> annosOp = pca.getAttackAdditionalInfo().stream()
							.filter(aai -> aai.getAnnotationType() == AttackAnnotationTypeEnum.learning_pattern)
							.map(AttackAdditionalInfo::getText)
							.findAny();
					gpami.setLearningPatternAnnos(annosOp.orElse(""));
					return gpami;
				})
				.toList();
		details.setLearnPokemonList(learnPokemonList);

		List<DispChargedAttack> sameTypeMoveList = movesUtils.convDispChargedAttackList(
				chargedAttackList.stream()
				.filter(ca -> ca.getType() == chargedAttack.getType())
				.toList());
		details.setSameTypeMoveList(sameTypeMoveList);

		return details;
	}

	private ChargedAttackRank createChargedAttackRank(String moveId, List<ChargedAttack> chargedAttackList) {

		ChargedAttackRank chargedAttackRank = new ChargedAttackRank();
		/* ジム・レイド */
		// ダメージ
		chargedAttackRank.setGymPowerRank(getRankDouble(moveId, ChargedAttack::getGymPower, chargedAttackList, true));
		// ゲージ増加量
		chargedAttackRank.setGymEnergyIncrAmountRank(getRankInt(moveId, ChargedAttack::getGymEnergyIncrAmount, chargedAttackList, true));
		// DPS
		chargedAttackRank.setDpsRank(getRankDouble(moveId, ChargedAttack::getDps, chargedAttackList, true));
		// 技の発生時間
		chargedAttackRank.setDamageSecondsRank(getRankInt(moveId, ChargedAttack::getDamageMs, chargedAttackList, false));
		/* PvP */
		// ダメージ
		chargedAttackRank.setPvpPowerRank(getRankDouble(moveId, ChargedAttack::getPvpPower, chargedAttackList, true));
		// ゲージ増加量
		chargedAttackRank.setPvpEnergyIncrAmountRank(getRankInt(moveId, ChargedAttack::getPvpEnergyIncrAmount, chargedAttackList, true));
		// DPE
		chargedAttackRank.setDpeRank(getRankDouble(moveId, ChargedAttack::getDpe, chargedAttackList, true));

		chargedAttackRank.setTotalCount(chargedAttackList.size());

		return chargedAttackRank;
	}

	/**
	 * 通常技orスペシャル技における順位を取得する。
	 * 要素と順位の対応は以下の通り<br>
	 * 1,2,3,3,4,5(要素の例)<br>
	 * 1,2,3,3,5,6(順位)
	 *
	 * @param <T> extends {@link Attack}
	 * @param moveId
	 * @param getFunc
	 * @param list
	 * @param shouldReverse
	 * @return
	 */
	private <T extends Attack> int getRankDouble(String moveId, ToDoubleFunction<T> getFunc, List<T> list, boolean shouldReverse) {

		Comparator<T> comparator = Comparator.comparingDouble(getFunc);
		if (shouldReverse) {
			comparator = comparator.reversed(); // 降順
		}
		List<T> sortedList = list.stream().sorted(comparator).toList();

		int totalCount = sortedList.size();
		double tempValue = getFunc.applyAsDouble(sortedList.get(0));
		int rank = 1;
		for (int i = 0; i < totalCount; i++) {
			T attack = sortedList.get(i);
			double value = getFunc.applyAsDouble(attack);
			if (value != tempValue) {
				// 1要素前と比べて同率でない場合、ランキングを繰り上げる
				rank = i + 1;
			}
			if (moveId.equals(attack.getMoveId())) {
				break;
			}
			tempValue = value;
		}
		return rank;
	}

	/**
	 * 通常技orスペシャル技における順位を取得する。
	 * 要素と順位の対応は以下の通り<br>
	 * 1,2,3,3,4,5(要素の例)<br>
	 * 1,2,3,3,5,6(順位)
	 *
	 * @param <T> extends {@link Attack}
	 * @param moveId
	 * @param sortedList
	 * @param getFunc
	 * @return
	 */
	private <T extends Attack> int getRankInt(String moveId, ToIntFunction<T> getFunc, List<T> list, boolean shouldReverse) {

		Comparator<T> comparator = Comparator.comparingInt(getFunc);
		if (shouldReverse) {
			comparator = comparator.reversed(); // 降順
		}
		List<T> sortedList = list.stream().sorted(comparator).toList();

		int totalCount = sortedList.size();
		int tempValue = getFunc.applyAsInt(sortedList.get(0));
		int rank = 1;
		for (int i = 0; i < totalCount; i++) {
			T attack = sortedList.get(i);
			int value = getFunc.applyAsInt(attack);
			if (value != tempValue) {
				// 1要素前と比べて同率でない場合、ランキングを繰り上げる
				rank = i + 1;
			}
			if (moveId.equals(attack.getMoveId())) {
				break;
			}
			tempValue = value;
		}
		return rank;
	}

}
