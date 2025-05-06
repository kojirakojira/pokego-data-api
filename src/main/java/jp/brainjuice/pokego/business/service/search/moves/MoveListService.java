package jp.brainjuice.pokego.business.service.search.moves;

import java.util.List;

import org.springframework.stereotype.Service;

import jp.brainjuice.pokego.business.service.search.utils.MovesUtils;
import jp.brainjuice.pokego.business.service.search.utils.dto.moves.DispChargedAttack;
import jp.brainjuice.pokego.business.service.search.utils.dto.moves.DispFastAttack;
import jp.brainjuice.pokego.dao.jpa.ChargedAttackRepository;
import jp.brainjuice.pokego.dao.jpa.FastAttackRepository;
import jp.brainjuice.pokego.dao.jpa.entity.ChargedAttack;
import jp.brainjuice.pokego.dao.jpa.entity.FastAttack;
import jp.brainjuice.pokego.web.search.form.res.moves.MoveListResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Service
public class MoveListService {

	private FastAttackRepository fastAttackRepository;

	private ChargedAttackRepository chargedAttackRepository;

	private MovesUtils movesUtils;

	public MoveListService(
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

	public void exec(MoveListResponse res) {

		List<FastAttack> faList = fastAttackRepository.findAll();
		List<DispFastAttack> dfaList = movesUtils.convDispFastAttackList(faList);
		res.setFaList(dfaList);

		List<ChargedAttack> caList = chargedAttackRepository.findAll();
		List<DispChargedAttack> dcaList = movesUtils.convDispChargedAttackList(caList);
		res.setCaList(dcaList);
	}
}
