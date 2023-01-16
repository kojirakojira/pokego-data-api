package jp.brainjuice.pokego.business.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;

import com.ibm.icu.text.MessageFormat;

import jp.brainjuice.pokego.business.dao.entity.Pokedex;
import jp.brainjuice.pokego.business.service.utils.memory.GenNameMap;
import jp.brainjuice.pokego.business.service.utils.memory.TypeMap;
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
@Component
@Slf4j
public class PokedexRepository implements CrudRepository<Pokedex, String> {

	private List<Pokedex> pokedexes;

	private TypeMap typeMap;
	private GenNameMap genNameMap;

	private static final String MSG_INVALID_TYPE_ERROR = "タイプの指定に誤りがあります。{0}";
	private static final String MSG_INVALID_GEN_ERROR = "世代の指定に誤りがあります。{0}";

	private static final String FILE_NAME = "pokemon.csv";

	@Autowired
	public PokedexRepository(
			TypeMap typeMap,
			GenNameMap genNameMap) {
		this.typeMap = typeMap;
		this.genNameMap = genNameMap;
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
	 * すべてのPokdexを取得します。
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
		List<Pokedex> pokedexList = new ArrayList<>();
		ids.forEach(id -> {
			Optional<Pokedex> pOp = ((List<Pokedex>) pokedexes).stream().filter(gp -> gp.getPokedexId().equals(id)).findAny();
			pOp.ifPresentOrElse(
					gp -> { pokedexList.add(gp); },
					() -> { pokedexList.add(null); });
		});
		return pokedexList;
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
	 * @deprecated 未実装
	 */
	@Override
	public <S extends Pokedex> Iterable<S> saveAll(Iterable<S> entities) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	/**
	 * 起動時に実行。CSVファイルの内容をメモリに抱える。
	 *
	 * @throws PokemonDataInitException
	 */
	@PostConstruct
	public void init() throws PokemonDataInitException {

		try {
			pokedexes = BjCsvMapper.mapping(FILE_NAME, Pokedex.class);

			// タイプが正しい値かをチェックする。
			checkType(pokedexes);
			// 世代が正しい値かチェックする。
			checkGen(pokedexes);

			log.info("Pokedex table generated!!");

		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new PokemonDataInitException(e);
		}
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
			if (!typeMap.containsKey(p.getType1())) {
				// タイプ１がタイプリストにない場合
				throw new PokemonDataInitException(MessageFormat.format(MSG_INVALID_TYPE_ERROR, p.toString()));
			}
			if (!p.getType2().isEmpty() && !typeMap.containsKey(p.getType2())) {
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
			if (!genNameMap.containsKey(p.getGen())) {
				// 世代が世代マップにない場合
				throw new PokemonDataInitException(MessageFormat.format(MSG_INVALID_GEN_ERROR, p.toString()));
			}
		}
	}
}
