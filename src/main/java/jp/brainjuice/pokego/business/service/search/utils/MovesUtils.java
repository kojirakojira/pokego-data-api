package jp.brainjuice.pokego.business.service.search.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import jp.brainjuice.pokego.business.constant.Type.TypeEnum;
import jp.brainjuice.pokego.business.service.search.utils.dto.moves.Buff;
import jp.brainjuice.pokego.business.service.search.utils.dto.moves.BuffContent;
import jp.brainjuice.pokego.business.service.search.utils.dto.moves.BuffContent.BuffTarget1Enum;
import jp.brainjuice.pokego.business.service.search.utils.dto.moves.BuffContent.BuffTarget2Enum;
import jp.brainjuice.pokego.business.service.search.utils.dto.moves.ChargedGymParam;
import jp.brainjuice.pokego.business.service.search.utils.dto.moves.ChargedPvpParam;
import jp.brainjuice.pokego.business.service.search.utils.dto.moves.DispChargedAttack;
import jp.brainjuice.pokego.business.service.search.utils.dto.moves.DispFastAttack;
import jp.brainjuice.pokego.business.service.search.utils.dto.moves.FastGymParam;
import jp.brainjuice.pokego.business.service.search.utils.dto.moves.FastPvpParam;
import jp.brainjuice.pokego.dao.jpa.entity.ChargedAttack;
import jp.brainjuice.pokego.dao.jpa.entity.FastAttack;
import jp.brainjuice.pokego.utils.BjUtils;
import lombok.Getter;

@Component
public class MovesUtils {

	private static final int MOVE_ID_LENGTH = 7;

	@Getter
	public enum MoveCode {
		fast_attack("1"), // 通常技のコード
		charged_attack("2"); // スペシャル技のコード

		private String code;

		MoveCode(String code) {
			this.code = code;
		}

		public static MoveCode lookup(String code) {
			return switch(code) {
			case "1" -> MoveCode.fast_attack;
			case "2" -> MoveCode.charged_attack;
			default -> throw new IllegalArgumentException("Unexpected value: " + code);
			};
		}
	}

	/**
	 * moveIdから通常技orスペシャル技を表すMoveCodeを取得する
	 *
	 * @param moveId
	 * @return
	 */
	public Optional<MoveCode> getMoveCode(String moveId) {

		if (StringUtils.isEmpty(moveId)) {
			return Optional.empty();
		}

		if (moveId.length() != MOVE_ID_LENGTH) {
			return Optional.empty();
		}

		String moveCodeStr = moveId.substring(3, 4);

		if (!Stream.of(MoveCode.values())
				.filter(mc -> moveCodeStr.equals(mc.getCode()))
				.anyMatch(e -> true)) {
			// moveIdの4桁目がMoveCodeに該当しなかった場合
			return Optional.empty();
		}

		return Optional.of(MoveCode.lookup(moveCodeStr));
	}

	public List<DispFastAttack> convDispFastAttackList(List<FastAttack> faList) {

		List<DispFastAttack> fastAttackList = faList.stream()
				.map(this::convDispFastAttack)
				.sorted((o1, o2) -> BjUtils.getCollator().compare(o1.getName(), o2.getName()))
				.collect(Collectors.toList());

		return IntStream.range(0, fastAttackList.size())
				.mapToObj(index -> {
					DispFastAttack dfa = fastAttackList.get(index);
					dfa.setNo(index + 1);
					return dfa;
				})
				.toList();
	}

	public List<DispChargedAttack> convDispChargedAttackList(List<ChargedAttack> caList) {

		List<DispChargedAttack> chargedAttackList = caList.stream()
				.map(this::convDispChargedAttack)
				.sorted((o1, o2) -> BjUtils.getCollator().compare(o1.getName(), o2.getName()))
				.collect(Collectors.toList());

		return IntStream.range(0, chargedAttackList.size())
				.mapToObj(index -> {
					DispChargedAttack dca = chargedAttackList.get(index);
					dca.setNo(index + 1);
					return dca;
				})
				.toList();
	}

	/**
	 * Entityの直列的な形式から、画面上で扱いやすい構造体の形に変換する。<br>
	 * Noは設定されない。
	 *
	 * @param {@link FastAttack} fa
	 * @return
	 */
	public DispFastAttack convDispFastAttack(FastAttack fa) {

		String moveId = fa.getMoveId();
		String name = fa.getName();
		TypeEnum type = fa.getType();
		FastGymParam gym = new FastGymParam(
				fa.getGymPower(),
				fa.getDps(),
				fa.getEps(),
				fa.getDamageMs() / 1000.0,
				fa.getTotalMs() / 1000.0);
		FastPvpParam pvp = new FastPvpParam(
				fa.getPvpPower(),
				fa.getPvpEnergyIncrAmount(),
				fa.getTurns(),
				fa.getDpt(),
				fa.getEpt());

		return new DispFastAttack(moveId, name, type, gym, pvp);
	}

	/**
	 * Entityの直列的な形式から、画面上で扱いやすい構造体の形に変換する。<br>
	 * Noは設定されない。
	 * @param {@link ChargedAttack} ca
	 * @return
	 */
	public DispChargedAttack convDispChargedAttack(ChargedAttack ca) {

		String moveId = ca.getMoveId();
		String name = ca.getName();
		TypeEnum type = ca.getType();
		ChargedGymParam gym = new ChargedGymParam(
				ca.getGymPower(),
				ca.getDps(),
				ca.getDamageMs() / 1000.0,
				ca.getTotalMs() / 1000.0,
				ca.getEnergyBar());
		ChargedPvpParam pvp = new ChargedPvpParam(
				ca.getPvpPower(),
				ca.getPvpEnergyIncrAmount(),
				ca.getDpe(),
				createBuff(ca));

		return new DispChargedAttack(moveId, name, type, gym, pvp);
	}

	private Buff createBuff(ChargedAttack ca) {

		List<BuffContent> buffContentList = new ArrayList<>();
		StringBuilder sb = new StringBuilder();

		if (ca.getOppAttackBuff() != 0) {
			appendBuffMsg(BuffTarget1Enum.opp, BuffTarget2Enum.attack, ca.getOppAttackBuff(), sb);
			buffContentList.add(new BuffContent(BuffTarget1Enum.opp, BuffTarget2Enum.attack, ca.getOppAttackBuff()));
		}
		if (ca.getOppDefenseBuff() != 0) {
			appendBuffMsg(BuffTarget1Enum.opp, BuffTarget2Enum.defense, ca.getOppDefenseBuff(), sb);
			buffContentList.add(new BuffContent(BuffTarget1Enum.opp, BuffTarget2Enum.defense, ca.getOppDefenseBuff()));
		}
		if (ca.getOwnAttackBuff() != 0) {
			appendBuffMsg(BuffTarget1Enum.own, BuffTarget2Enum.attack, ca.getOwnAttackBuff(), sb);
			buffContentList.add(new BuffContent(BuffTarget1Enum.own, BuffTarget2Enum.attack, ca.getOwnAttackBuff()));
		}
		if (ca.getOwnDefenseBuff() != 0) {
			appendBuffMsg(BuffTarget1Enum.own, BuffTarget2Enum.defense, ca.getOwnDefenseBuff(), sb);
			buffContentList.add(new BuffContent(BuffTarget1Enum.own, BuffTarget2Enum.defense, ca.getOwnDefenseBuff()));
		}

		String buffMsg = sb.toString();
		if (!StringUtils.isEmpty(buffMsg)) {
			// 一番後ろの改行コードを除去する
			buffMsg = buffMsg.substring(0, buffMsg.length() - 1);
		} else {
			buffMsg = "-";
		}

		String activationChanceStr = ca.getActivationChance() > 0.0
				? String.valueOf(ca.getActivationChance() * 100.0) + "%"
				: "-";

		return new Buff(buffContentList, buffMsg, ca.getActivationChance(), activationChanceStr);
	}

	private void appendBuffMsg(BuffTarget1Enum target1, BuffTarget2Enum target2, int effect, StringBuilder sb) {

		if (effect != 0) {
			String msg1 = switch (target1) {
			case opp -> "相手";
			case own -> "自分";
			};
			sb.append(msg1);
			String msg2 = switch (target2) {
			case attack -> "こうげき";
			case defense -> "ぼうぎょ";
			};
			sb.append(msg2);
			if (effect > 0) {
				IntStream.range(0, effect).forEach((num) -> sb.append("△"));
			} else {
				IntStream.range(effect, 0).forEach((num) -> sb.append("▽"));
			}
			sb.append("\n");
		}
	}

}
