package jp.brainjuice.pokego.business.dao;

import java.text.MessageFormat;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.core.io.Resource;
import org.springframework.stereotype.Repository;

import jp.brainjuice.pokego.business.constant.Type.TypeEnum;
import jp.brainjuice.pokego.business.dao.entity.FastAttack;
import jp.brainjuice.pokego.utils.BjCsvMapper;
import jp.brainjuice.pokego.utils.BjUtils;
import jp.brainjuice.pokego.utils.exception.PokemonDataInitException;
import jp.brainjuice.pokego.utils.external.AwsS3Utils;
import lombok.extern.slf4j.Slf4j;

/**
 * 原作におけるポケモンの情報を取得するRepositoryクラス
 *
 * @author saibabanagchampa
 *
 */
@Repository
@Slf4j
public class FastAttackRepository extends InMemoryRepository<FastAttack, String> {

	private AwsS3Utils awsS3Utils;

	private static final String MSG_FORMAT_ERROR = "{0}の指定に誤りがあります。（技名：{1}, 行：{2}, 値：{3}）";

	/** fast-attacks.csv */
	private static final String FAST_ATTACKS_CSV_FILE_NAME = "pokemon/moves/fast-attacks.csv";

	/**
	 * CSVファイルからFastAttackを生成し、DIに登録する。
	 *
	 * @param typeMap
	 * @param genNameMap
	 * @throws PokemonDataInitException
	 */
	public FastAttackRepository(AwsS3Utils awsS3Utils) throws PokemonDataInitException {
		this.awsS3Utils = awsS3Utils;
		init();
	}

	/**
	 * 主キーはmoveId
	 */
	@Override
	protected String getKey(FastAttack t) {
		return t.getMoveId();
	}

	/**
	 * 起動時に実行。CSVファイルの内容をメモリに抱える。
	 *
	 * @throws PokemonDataInitException
	 */
	public void init() throws PokemonDataInitException {

		String fileName = FAST_ATTACKS_CSV_FILE_NAME;
		try {
//			fileName = MessageFormat.format(S3_POKEMON_CSV_FILE_NAME, awsS3Utils.getSuffix());
//			S3Object object = awsS3Utils.download(fileName);
			Resource resource = BjUtils.loadFile(fileName);
			List<FastAttack> attackList = (List<FastAttack>) saveAll(BjCsvMapper.mapping(resource, FastAttack.class));

			checkFormat(attackList);

		} catch (PokemonDataInitException pde) {
			log.error(pde.getMessage(), pde);
			throw pde;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new PokemonDataInitException(e);
		}

		log.info(MessageFormat.format("Moves(Fast Attack) table generated!! (Referenced file: {0}{1})", awsS3Utils.getEndpoint(), fileName));
	}

	/*
	 * ファイルの整合性チェック
	 *
	 * @param attackList
	 * @throws PokemonDataInitException
	 */
	private void checkFormat(List<FastAttack> attackList) throws PokemonDataInitException {

		for (FastAttack fa: attackList) {

			// 技ID
			Pattern pattern = Pattern.compile("^[A-Z]{2}1[0-9]{3}$");
			if (!pattern.matcher(fa.getMoveId()).find()) {
				throw new PokemonDataInitException(
						MessageFormat.format(MSG_FORMAT_ERROR, "技ID", fa.getName(), attackList.indexOf(fa) + 1, fa.getMoveId()));
			}

			// タイプ
			if (!TypeEnum.isDefined(fa.getType())) {
				throw new PokemonDataInitException(
						MessageFormat.format(MSG_FORMAT_ERROR, "タイプ", fa.getName(), attackList.indexOf(fa) + 1, fa.getType()));
			}

			// DPS
			float gymPower = (float) fa.getGymPower();
			float calcedDps = BjUtils.round(gymPower, fa.getTotalTime(), 3);
			if (Float.compare(calcedDps, fa.getDps()) != 0) {
				throw new PokemonDataInitException(
						MessageFormat.format(MSG_FORMAT_ERROR, "DPS", fa.getName(), attackList.indexOf(fa) + 1, fa.getDps()));
			}

			// DPT
			float pvpPower = (float) fa.getPvpPower();
			float turns = (float) fa.getTurns();
			float calcedDpt = BjUtils.round(pvpPower, turns, 3);
			if (Float.compare(calcedDpt, fa.getDpt()) != 0) {
				throw new PokemonDataInitException(
						MessageFormat.format(MSG_FORMAT_ERROR, "DPT", fa.getName(), attackList.indexOf(fa) + 1, fa.getDpt()));
			}

			// EPT
			float energyIncrAmount = (float) fa.getEnergyIncrAmount();
			float calcedEpt = BjUtils.round(energyIncrAmount, turns, 3);
			if (Float.compare(calcedEpt, fa.getEpt()) != 0) {
				throw new PokemonDataInitException(
						MessageFormat.format(MSG_FORMAT_ERROR, "EPT", fa.getName(), attackList.indexOf(fa) + 1, fa.getEpt()));
			}

			log.debug("Check OK. " + fa);
		}
	}
}
