package jp.brainjuice.pokego.business.service.search.moves;

import java.util.List;

import org.springframework.stereotype.Service;

import jp.brainjuice.pokego.business.constant.Type.TypeEnum;
import jp.brainjuice.pokego.business.service.search.utils.MovesUtils;
import jp.brainjuice.pokego.business.service.search.utils.dto.moves.DispChargedAttack;
import jp.brainjuice.pokego.business.service.search.utils.dto.moves.DispFastAttack;
import jp.brainjuice.pokego.dao.jpa.ChargedAttackRepository;
import jp.brainjuice.pokego.dao.jpa.FastAttackRepository;
import jp.brainjuice.pokego.dao.jpa.entity.ChargedAttack;
import jp.brainjuice.pokego.dao.jpa.entity.FastAttack;
import jp.brainjuice.pokego.web.search.form.req.moves.FilterAllMoveRequest;
import jp.brainjuice.pokego.web.search.form.res.moves.FilterAllMoveResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Service
public class FilterAllMoveService {

	private FastAttackRepository fastAttackRepository;

	private ChargedAttackRepository chargedAttackRepository;

	private MovesUtils movesUtils;

	public FilterAllMoveService(
			FastAttackRepository fastAttackRepository,
			ChargedAttackRepository chargedAttackRepository,
			MovesUtils movesUtils) {
		this.fastAttackRepository = fastAttackRepository;
		this.chargedAttackRepository = chargedAttackRepository;
		this.movesUtils = movesUtils;
	}

	@AllArgsConstructor
	public enum MoveListPatternEnum {
		gym("ジム・レイド"),
		pvp("PvP")
		;

		@Getter
		private String jpn;
	}

	public enum MoveDispTypeEnum {
		all, // ALL
		fa, // FastAttack
		ca, // ChargedAttack
		;
	}

	public void exec(FilterAllMoveRequest req, FilterAllMoveResponse res) {

		List<TypeEnum> typeList = req.getTypes();
		int typeLength = TypeEnum.values().length;

		// typesの指定がない場合、または全てor通常技を選択したときのみ、通常技のリストを返却する。
		boolean shouldDispFaList = typeList == null || List.of(MoveDispTypeEnum.all, MoveDispTypeEnum.fa).contains(req.getMoveDispType());
		res.setShouldDispFaList(shouldDispFaList);
		if (shouldDispFaList) {
			res.setFaList(getFaList(typeList, typeLength));
		}


		// typesの指定がない場合、全てorスペシャル技を選択したときのみ、スペシャル技のリストを返却する。
		boolean shouldDispCaList = typeList == null || List.of(MoveDispTypeEnum.all, MoveDispTypeEnum.ca).contains(req.getMoveDispType());
		res.setShouldDispCaList(shouldDispCaList);
		if (shouldDispCaList) {
			res.setCaList(getCaList(typeList, typeLength));
		}
	}

	private List<DispFastAttack> getFaList(List<TypeEnum> typeList, int typeLength) {

		List<FastAttack> faList;
		if (typeList == null || typeList.size() == typeLength) {
			// すべてのタイプが選択された場合
			faList = fastAttackRepository.findAll();
		} else {
			faList = fastAttackRepository.findByTypeIn(typeList);
		}

		List<DispFastAttack> dfaList = movesUtils.convDispFastAttackList(faList);

		return dfaList;
	}

	private List<DispChargedAttack> getCaList(List<TypeEnum> typeList, int typeLength) {

		List<ChargedAttack> caList;
		if (typeList == null || typeList.size() == typeLength) {
			// すべてのタイプが選択された場合
			caList = chargedAttackRepository.findAll();
		} else {
			caList = chargedAttackRepository.findByTypeIn(typeList);
		}

		List<DispChargedAttack> dcaList = movesUtils.convDispChargedAttackList(caList);

		return dcaList;
	}
}
