package jp.brainjuice.pokego.business.dao;

import java.text.MessageFormat;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Repository;

import com.amazonaws.services.s3.model.S3Object;

import jp.brainjuice.pokego.business.constant.GenNameEnum;
import jp.brainjuice.pokego.business.constant.Type.TypeEnum;
import jp.brainjuice.pokego.business.dao.entity.Pokedex;
import jp.brainjuice.pokego.utils.BjCsvMapper;
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
public class PokedexRepository extends InMemoryRepository<Pokedex, String> {

	private AwsS3Utils awsS3Utils;

	private static final String MSG_INVALID_TYPE_ERROR = "タイプの指定に誤りがあります。{0}";
	private static final String MSG_INVALID_GEN_ERROR = "世代の指定に誤りがあります。{0}";

	/** pokemon.csv */
	private static final String S3_POKEMON_CSV_FILE_NAME = "pokego/data/pokemon{0}.csv";

	/**
	 * CSVファイルからPokedexを生成し、DIに登録する。
	 *
	 * @param typeMap
	 * @param genNameMap
	 * @throws PokemonDataInitException
	 */
	@Autowired
	public PokedexRepository(AwsS3Utils awsS3Utils) throws PokemonDataInitException {
		this.awsS3Utils = awsS3Utils;
		init();
	}

	/**
	 * 主キーはpokedexId
	 */
	@Override
	protected String getKey(Pokedex t) {
		return t.getPokedexId();
	}

	/**
	 * 起動時に実行。CSVファイルの内容をメモリに抱える。
	 *
	 * @throws PokemonDataInitException
	 */
	public void init() throws PokemonDataInitException {

		String fileName = "";
		try {
			fileName = MessageFormat.format(S3_POKEMON_CSV_FILE_NAME, awsS3Utils.getSuffix());
			S3Object object = awsS3Utils.download(fileName);
			Resource resource = new InputStreamResource(object.getObjectContent());
			List<Pokedex> pokedexes = (List<Pokedex>) saveAll(BjCsvMapper.mapping(resource, Pokedex.class));

			// タイプが正しい値かをチェックする。
			checkType(pokedexes);
			// 世代が正しい値かチェックする。
			checkGen(pokedexes);

		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new PokemonDataInitException(e);
		}

		log.info(MessageFormat.format("Pokedex table generated!! (Referenced file: {0}{1})", awsS3Utils.getEndpoint(), fileName));
	}

	/**
	 * タイプの設定が正しいか確認します。<br>
	 * 正しくない場合はPokemonDataInitExceptionをスローします。
	 *
	 * @param pokedexList
	 * @return
	 */
	private void checkType(List<Pokedex> pokedexList) throws PokemonDataInitException {

		for (Pokedex p: pokedexList) {
			if (TypeEnum.getType(p.getType1()) == null) {
				// タイプ１がタイプリストにない場合
				throw new PokemonDataInitException(MessageFormat.format(MSG_INVALID_TYPE_ERROR, p.toString()));
			}
			if (!p.getType2().isEmpty() && TypeEnum.getType(p.getType2()) == null) {
				// タイプ２が空でないかつ、タイプリストにない場合
				throw new PokemonDataInitException(MessageFormat.format(MSG_INVALID_TYPE_ERROR, p.toString()));
			}
		}
	}

	/**
	 * 世代の設定が正しいか確認します。<br>
	 * 正しくない場合はPokemonDataInitExceptionをスローします。
	 *
	 * @param pokedexList
	 * @return
	 */
	private void checkGen(List<Pokedex> pokedexList) throws PokemonDataInitException {

		for (Pokedex p: pokedexList) {
			if (GenNameEnum.valueOf(p.getGen()) == null) {
				// 世代が世代マップにない場合
				throw new PokemonDataInitException(MessageFormat.format(MSG_INVALID_GEN_ERROR, p.toString()));
			}
		}
	}
}
