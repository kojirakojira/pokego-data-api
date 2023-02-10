package jp.brainjuice.pokego.business.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.ibm.icu.text.MessageFormat;

import jp.brainjuice.pokego.business.constant.GenNameEnum;
import jp.brainjuice.pokego.business.constant.Type.TypeEnum;
import jp.brainjuice.pokego.business.dao.entity.Pokedex;
import jp.brainjuice.pokego.utils.BjCsvMapper;
import jp.brainjuice.pokego.utils.exception.PokemonDataInitException;
import lombok.extern.slf4j.Slf4j;

/**
 * ポケモン情報をCSVから取得し、メモリに保持し管理するRepositoryです。<br>
 * Spring Data Jpaに近い仕様を目指してますが、ほぼハリボテです。
 *
 * @author saibabanagchampa
 *
 */
@Repository
@Slf4j
public class PokedexRepository implements CrudRepository<Pokedex, String> {

	private final List<Pokedex> pokedexes = new ArrayList<Pokedex>();

	private static final String MSG_INVALID_TYPE_ERROR = "タイプの指定に誤りがあります。{0}";
	private static final String MSG_INVALID_GEN_ERROR = "世代の指定に誤りがあります。{0}";

	private static final String FILE_NAME = "pokemon.csv";

	/**
	 * CSVファイルからPokedexを生成し、DIに登録する。
	 *
	 * @param typeMap
	 * @param genNameMap
	 * @throws PokemonDataInitException
	 */
	@Autowired
	public PokedexRepository() throws PokemonDataInitException {
		init();
	}

	/**
	 * 図鑑№からPokedexを検索します。
	 *
	 * @param pokedexId
	 * @return
	 */
	public Optional<Pokedex> findById(String pokedexId) {

		Optional<Pokedex> pokedexOp = Optional.empty();

		if (StringUtils.isEmpty(pokedexId)) return pokedexOp;

		for (Pokedex p: pokedexes) {
			if (pokedexId.equals(p.getPokedexId())) {
				pokedexOp = Optional.of(p.clone());
				break;
			}
		}

		return pokedexOp;
	}

	/**
	 * すべてのPokedexを取得します。
	 *
	 * @return
	 */
	public List<Pokedex> findAll() {
		return new ArrayList<>(pokedexes);
	}

	/**
	 * 図鑑№のリストからPokedexを検索します。
	 *
	 * @param ids
	 * @return
	 */
	@Override
	public Iterable<Pokedex> findAllById(Iterable<String> ids) {
		return pokedexes.stream().filter(p -> {
			boolean exists = false;
			for (String pid: ids) {
				exists = p.getPokedexId().equals(pid);
				if (exists) break;
			}
			return exists;
		}).collect(Collectors.toList());
	}

	/**
	 * @deprecated 未実装
	 */
	@Override
	public long count() {
		// TODO 自動生成されたメソッド・スタブ
		return 0;
	}

	/**
	 * @deprecated 未実装
	 */
	@Override
	public void delete(Pokedex entity) {
		// TODO 自動生成されたメソッド・スタブ

	}

	/**
	 * @deprecated 未実装
	 */
	@Override
	public void deleteAll() {
		// TODO 自動生成されたメソッド・スタブ

	}

	/**
	 * @deprecated 未実装
	 */
	@Override
	public void deleteAll(Iterable<? extends Pokedex> entities) {
		// TODO 自動生成されたメソッド・スタブ

	}

	/**
	 * @deprecated 未実装
	 */
	@Override
	public void deleteAllById(Iterable<? extends String> ids) {
		// TODO 自動生成されたメソッド・スタブ

	}

	/**
	 * @deprecated 未実装
	 */
	@Override
	public void deleteById(String id) {
		// TODO 自動生成されたメソッド・スタブ

	}

	/**
	 * @deprecated 未実装
	 */
	@Override
	public boolean existsById(String id) {
		// TODO 自動生成されたメソッド・スタブ
		return false;
	}

	/**
	 * @deprecated 未実装
	 */
	@Override
	public <S extends Pokedex> S save(S entity) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	/**
	 * すべてのPokedexを保存します。
	 *
	 * @param <S>
	 * @param entities
	 * @return
	 */
	@Override
	public <S extends Pokedex> Iterable<S> saveAll(Iterable<S> entities) {
		entities.forEach(pokedexes::add);
		return entities;
	}

	/**
	 * 起動時に実行。CSVファイルの内容をメモリに抱える。
	 *
	 * @throws PokemonDataInitException
	 */
	public void init() throws PokemonDataInitException {

		try {
			List<Pokedex> pokedexes = (List<Pokedex>) saveAll(BjCsvMapper.mapping(FILE_NAME, Pokedex.class));

			// タイプが正しい値かをチェックする。
			checkType(pokedexes);
			// 世代が正しい値かチェックする。
			checkGen(pokedexes);

		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new PokemonDataInitException(e);
		}

		log.info("Pokedex table generated!!");
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
