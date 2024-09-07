package jp.brainjuice.pokego.business.dao;

import java.text.MessageFormat;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Repository;

import jp.brainjuice.pokego.business.constant.Type.TypeEnum;
import jp.brainjuice.pokego.business.dao.entity.ChargedAttack;
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
public class ChargedAttackRepository extends InMemoryRepository<ChargedAttack, String> {

	private AwsS3Utils awsS3Utils;

	private static final String MSG_FORMAT_ERROR = "{0}の指定に誤りがあります。（技名：{1}, 行：{2}, 値：{3}）";

	private static final String MSG_FORMAT_RELATION_ERROR = "相関エラーがあります。{0}（技名：{1}, 行：{2}）";

	/** charged-attacks.csv */
	private static final String CHARGED_ATTACKS_CSV_FILE_NAME = "pokemon/moves/charged-attacks.csv";

	/**
	 * CSVファイルからChargedAttackを生成し、DIに登録する。
	 *
	 * @param typeMap
	 * @param genNameMap
	 * @throws PokemonDataInitException
	 */
	public ChargedAttackRepository(AwsS3Utils awsS3Utils) throws PokemonDataInitException {
		this.awsS3Utils = awsS3Utils;
		init();
	}

	/**
	 * 主キーはmoveId
	 */
	@Override
	protected String getKey(ChargedAttack t) {
		return t.getMoveId();
	}

	/**
	 * 起動時に実行。CSVファイルの内容をメモリに抱える。
	 *
	 * @throws PokemonDataInitException
	 */
	public void init() throws PokemonDataInitException {

		String fileName = CHARGED_ATTACKS_CSV_FILE_NAME;
		try {
//			fileName = MessageFormat.format(S3_POKEMON_CSV_FILE_NAME, awsS3Utils.getSuffix());
//			S3Object object = awsS3Utils.download(fileName);
			Resource resource = BjUtils.loadFile(fileName);
			List<ChargedAttack> attackList = (List<ChargedAttack>) saveAll(BjCsvMapper.mapping(resource, ChargedAttack.class));

			checkFormat(attackList);

		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new PokemonDataInitException(e);
		}

		log.info(MessageFormat.format("Moves(Charged Attack) table generated!! (Referenced file: {0}{1})", awsS3Utils.getEndpoint(), fileName));
	}

	/*
	 * ファイルの整合性チェック
	 *
	 * @param attackList
	 * @throws PokemonDataInitException
	 */
	private void checkFormat(List<ChargedAttack> attackList) throws PokemonDataInitException {

		for (ChargedAttack ca: attackList) {

			// 技ID
			Pattern pattern = Pattern.compile("^[A-Z]{2}2[0-9]{3}$");
			if (!pattern.matcher(ca.getMoveId()).find()) {
				throw new PokemonDataInitException(
						MessageFormat.format(MSG_FORMAT_ERROR, "技ID", ca.getName(), attackList.indexOf(ca) + 1, ca.getMoveId()));
			}

			// タイプ
			if (!TypeEnum.isDefined(ca.getType())) {
				throw new PokemonDataInitException(
						MessageFormat.format(MSG_FORMAT_ERROR, "タイプ", ca.getName(), attackList.indexOf(ca) + 1, ca.getType()));
			}

			// DPS
			float gymPower = (float) ca.getGymPower();
			float calcedDps = BjUtils.round(gymPower, ca.getTotalTime(), 3);
			if (Float.compare(calcedDps, ca.getDps()) != 0) {
				throw new PokemonDataInitException(
						MessageFormat.format(MSG_FORMAT_ERROR, "DPS", ca.getName(), attackList.indexOf(ca) + 1, ca.getDps()));
			}

			// ゲージ本数（1～3以外はエラー）
			if (!(1 <= ca.getEnergyBar() && ca.getEnergyBar() <= 3)) {
				throw new PokemonDataInitException(
						MessageFormat.format(MSG_FORMAT_ERROR, "ゲージ本数(energyBar)", ca.getName(), attackList.indexOf(ca) + 1, ca.getEnergyBar()));
			}

			// ゲージ増加量（マイナスでなければエラー）
			if (!(ca.getEnergyIncrAmount() < 0)) {
				throw new PokemonDataInitException(
						MessageFormat.format(
								MSG_FORMAT_ERROR,
								"ゲージ増加量(energyIncrAmount)",
								ca.getName(),
								attackList.indexOf(ca) + 1,
								ca.getEnergyBar() + "(マイナス値が正)"));
			}

			// DPT
			float pvpPower = (float) ca.getPvpPower();
			float energyIncrAmount = (float) ca.getEnergyIncrAmount();
			float calcedDpt = BjUtils.round(pvpPower, Math.abs(energyIncrAmount), 3);
			if (Float.compare(calcedDpt, ca.getDpe()) != 0) {
				throw new PokemonDataInitException(
						MessageFormat.format(MSG_FORMAT_ERROR, "DPT", ca.getName(), attackList.indexOf(ca) + 1, ca.getDpe()));
			}

			checkRelation(ca, attackList);

			log.debug("Check OK. " + ca);
		}
	}

	private void checkRelation(ChargedAttack ca, List<ChargedAttack> attackList) throws PokemonDataInitException {

		// バフ・デバフ
		if (!StringUtils.isEmpty(ca.getBuffTarget1()) && ca.getActivationChance() == null) {
			throw new PokemonDataInitException(
					MessageFormat.format(
							MSG_FORMAT_RELATION_ERROR,
							"buffTarget1に入力がある場合、activationChanceは必須",
							ca.getName(),
							attackList.indexOf(ca) + 1));
		}

		if (!StringUtils.isEmpty(ca.getBuffTarget1()) && ca.getBuffEffect1() == null) {
			throw new PokemonDataInitException(
					MessageFormat.format(
							MSG_FORMAT_RELATION_ERROR,
							"buffTarget1に入力がある場合、buffEfect1は必須",
							ca.getName(),
							attackList.indexOf(ca) + 1));
		}

		if (!StringUtils.isEmpty(ca.getBuffTarget2()) && StringUtils.isEmpty(ca.getBuffTarget1())) {
			throw new PokemonDataInitException(
					MessageFormat.format(
							MSG_FORMAT_RELATION_ERROR,
							"buffTarget2に入力がある場合、buffTarget1は必須",
							ca.getName(),
							attackList.indexOf(ca) + 1));
		}

		if (!StringUtils.isEmpty(ca.getBuffTarget2()) && ca.getBuffEffect2() == null) {
			throw new PokemonDataInitException(
					MessageFormat.format(
							MSG_FORMAT_RELATION_ERROR,
							"buffTarget2に入力がある場合、buffEffect2は必須",
							ca.getName(),
							attackList.indexOf(ca) + 1));
		}
	}
}
