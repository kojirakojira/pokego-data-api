package jp.brainjuice.pokego.business.service.research.cp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jp.brainjuice.pokego.business.dao.entity.GoPokedex;
import jp.brainjuice.pokego.business.service.research.ResearchService;
import jp.brainjuice.pokego.business.service.utils.PokemonGoUtils;
import jp.brainjuice.pokego.business.service.utils.dto.IndividialValue;
import jp.brainjuice.pokego.web.form.res.research.cp.RaidResponse;

@Service
public class RaidResearchService implements ResearchService<RaidResponse> {

	private PokemonGoUtils pokemonGoUtils;

	/** レイド固定値最高 */
	private static final int RAID_MAX_IV = 15;

	/** レイド個体値最低 */
	private static final int RAID_MIN_IV = 10;

	/** 通常時PL */
	private static final String RAID_PL = "20";

	/** 天候ブースト時PL */
	private static final String RAID_PL_WB = "25";

	@Autowired
	public RaidResearchService(PokemonGoUtils pokemonGoUtils) {
		this.pokemonGoUtils = pokemonGoUtils;
	}

	@Override
	public void exec(IndividialValue iv, RaidResponse res) {

		GoPokedex goPokedex = iv.getGoPokedex();
		res.setMaxCp(pokemonGoUtils.culcCp(goPokedex, RAID_MAX_IV, RAID_MAX_IV, RAID_MAX_IV, RAID_PL));
		res.setMinCp(pokemonGoUtils.culcCp(goPokedex, RAID_MIN_IV, RAID_MIN_IV, RAID_MIN_IV, RAID_PL));
		res.setWbMaxCp(pokemonGoUtils.culcCp(goPokedex, RAID_MAX_IV, RAID_MAX_IV, RAID_MAX_IV, RAID_PL_WB));
		res.setWbMinCp(pokemonGoUtils.culcCp(goPokedex, RAID_MIN_IV, RAID_MIN_IV, RAID_MIN_IV, RAID_PL_WB));

		res.setMessage("成功！！");
	}

}
