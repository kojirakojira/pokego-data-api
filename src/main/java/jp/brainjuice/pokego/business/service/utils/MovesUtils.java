package jp.brainjuice.pokego.business.service.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import jp.brainjuice.pokego.business.constant.Type.TypeEnum;
import jp.brainjuice.pokego.business.dao.entity.ChargedAttack;
import jp.brainjuice.pokego.business.dao.entity.FastAttack;
import jp.brainjuice.pokego.business.service.utils.dto.moves.Buff;
import jp.brainjuice.pokego.business.service.utils.dto.moves.BuffContent;
import jp.brainjuice.pokego.business.service.utils.dto.moves.ChargedGymParam;
import jp.brainjuice.pokego.business.service.utils.dto.moves.ChargedPvpParam;
import jp.brainjuice.pokego.business.service.utils.dto.moves.DispChargedAttack;
import jp.brainjuice.pokego.business.service.utils.dto.moves.DispFastAttack;
import jp.brainjuice.pokego.business.service.utils.dto.moves.FastGymParam;
import jp.brainjuice.pokego.business.service.utils.dto.moves.FastPvpParam;

@Component
public class MovesUtils {

	/** ChargedAttackのbuffTargetはハイフン区切りで表す。 */
	private static String BUFF_TARGET_SEPARATOR = "-";

	/** ハイフンの前 */
	public enum BuffTarget1Enum {
		own,
		opp
	}

	/** ハイフンの後ろ */
	public enum BuffTarget2Enum {
		attack,
		defense
	}

	public List<DispFastAttack> convDispFastAttackList(List<FastAttack> faList) {

		return faList.stream()
				.map(this::convDispFastAttack)
				.collect(Collectors.toList());
	}

	public List<DispChargedAttack> convDispChargedAttackList(List<ChargedAttack> faList) {

		return faList.stream()
				.map(this::convDispChargedAttack)
				.collect(Collectors.toList());
	}

	/**
	 * Entityの直列的な形式から、画面上で扱いやすい構造体の形に変換する。
	 * @param {@link FastAttack} fa
	 * @return
	 */
	public DispFastAttack convDispFastAttack(FastAttack fa) {

		String moveId = fa.getMoveId();
		String name = fa.getName();
		TypeEnum type = TypeEnum.valueOf(fa.getType());
		FastGymParam gym = new FastGymParam(fa.getGymPower(), fa.getDps(), fa.getEps(), fa.getDamagedTime(), fa.getTotalTime());
		FastPvpParam pvp = new FastPvpParam(fa.getPvpPower(), fa.getEnergyIncrAmount(), fa.getTurns(), fa.getDpt(), fa.getEpt());

		return new DispFastAttack(moveId, name, type, gym, pvp);
	}

	/**
	 * Entityの直列的な形式から、画面上で扱いやすい構造体の形に変換する。
	 * @param {@link ChargedAttack} ca
	 * @return
	 */
	public DispChargedAttack convDispChargedAttack(ChargedAttack ca) {

		String moveId = ca.getMoveId();
		String name = ca.getName();
		TypeEnum type = TypeEnum.valueOf(ca.getType());
		ChargedGymParam gym = new ChargedGymParam(ca.getGymPower(), ca.getDps(), ca.getDamagedTime(), ca.getTotalTime(), ca.getEnergyBar());
		ChargedPvpParam pvp = new ChargedPvpParam(ca.getPvpPower(), ca.getEnergyIncrAmount(), ca.getDpe(), createBuff(ca));

		return new DispChargedAttack(moveId, name, type, gym, pvp);
	}

	private Buff createBuff(ChargedAttack ca) {

		if (StringUtils.isEmpty(ca.getBuffTarget1())) {
			return null;
		}

		List<BuffContent> buffContentList = new ArrayList<>(2);

		// バフ・デバフ１を追加
		buffContentList.add(createBuffContent(ca.getBuffTarget1(), ca.getBuffEffect1()));

		if (!StringUtils.isEmpty(ca.getBuffTarget2())) {
			// バフ・デバフ２を追加
			buffContentList.add(createBuffContent(ca.getBuffTarget2(), ca.getBuffEffect2()));
		}

		return new Buff(buffContentList, ca.getActivationChance());
	}

	private BuffContent createBuffContent(String target, int effect) {
		int idx = target.indexOf(BUFF_TARGET_SEPARATOR);
		BuffTarget1Enum t1 = BuffTarget1Enum.valueOf(target.substring(0, idx));
		BuffTarget2Enum t2 = BuffTarget2Enum.valueOf(target.substring(idx + 1));

		return new BuffContent(t1, t2, effect);
	}
}
