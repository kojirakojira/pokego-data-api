package jp.brainjuice.pokego.business.dao;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;

import com.ibm.icu.text.MessageFormat;

import jp.brainjuice.pokego.business.dao.entity.Pokedex;
import jp.brainjuice.pokego.business.service.utils.memory.GenNameMap;
import jp.brainjuice.pokego.business.service.utils.memory.TypeMap;
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

	private static final String MSG_METHOD_INVOKE_ERROR = "カラム名の指定、もしくはデータの型に誤りがあります。行番号:{0}, 列番号:{1}";
	private static final String MSG_INDEX_ERRPR = "列の個数が一致しませんでした。行番号:{0}, 列番号:{1}";
	private static final String MSG_INVALID_TYPE_ERROR = "タイプの指定に誤りがあります。{0}";
	private static final String MSG_INVALID_GEN_ERROR = "世代の指定に誤りがあります。{0}";

	private static final String FILE_NAME = "pokemon.csv";
	private static final String SEPARATOR = ",";

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
	 * @throws Exception
	 */
	@PostConstruct
	public void init() throws Exception {

		try {
			DefaultResourceLoader resourceLoader = new DefaultResourceLoader();
			Resource resource = resourceLoader.getResource("classpath:" + FILE_NAME);
			BufferedReader br = new BufferedReader(new InputStreamReader(resource.getInputStream()));

			// 行のリスト
			ArrayList<String> rowList = new ArrayList<String>();
			String text;
			while ((text = br.readLine()) != null) {
				rowList.add(text);
			}

			pokedexes = convPokedexList(rowList);

			// タイプが正しい値かをチェックする。
			checkType(pokedexes);
			// 世代が正しい値かチェックする。
			checkGen(pokedexes);

			log.info("Pokedex table generated!!");
		} catch (PokemonDataInitException e) {
			log.error(e.getMessage());
			throw e;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new PokemonDataInitException(e);
		}
	}

	/**
	 * CSV形式の行のリストをPokedexのリストに変換します。<br>
	 * List<String> → List<Pokedex>
	 *
	 * @param rowList
	 * @return
	 * @throws Exception
	 */
	private List<Pokedex> convPokedexList(ArrayList<String> rowList) throws Exception {

		List<Pokedex> pokedexList = new ArrayList<>();

		// メソッド名のリストを作成する。csvファイルの1行目はカラム名（キャメルケース）
		ArrayList<String> setterList = new ArrayList<String>();
		for (String col: rowList.get(0).split(SEPARATOR, -1)) {
			String methodName = "set" + col.substring(0, 1).toUpperCase() + col.substring(1);
			setterList.add(methodName);
		}

		int x = 0;
		int y = 0;
		try {
			for (y = 1; y < rowList.size(); y++) {
				Pokedex pokedex = new Pokedex();

				// Pokedexに各項目を設定する。
				String[] colArr = rowList.get(y).split(SEPARATOR, -1); // 末尾の空文字を除去しない。
				if (colArr.length != setterList.size()) {
					// 1行目と対象の行が一致していない場合
					x = colArr.length;
					throw new ArrayIndexOutOfBoundsException();
				}

				for (x = 0; x < colArr.length; x++) {
					// セッターを実行
					invokeSetter(pokedex, colArr[x], setterList.get(x));
				}

				pokedexList.add(pokedex);
			}
		} catch (NoSuchMethodException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			log.error(MessageFormat.format(MSG_METHOD_INVOKE_ERROR, y + 1, x));
			throw new PokemonDataInitException(e);
		} catch (ArrayIndexOutOfBoundsException e) {
			log.error(MessageFormat.format(MSG_INDEX_ERRPR, y + 1, x));
			throw new PokemonDataInitException(e);
		}

		return pokedexList;
	}

	/**
	 * Pokedexのセッターを実行します。
	 *
	 * @param colValue
	 * @param methodName
	 * @param pokedex
	 * @throws Exception
	 */
	private void invokeSetter(Pokedex pokedex, String colValue, String methodName) throws Exception {

		Class<?> clazz = String.class;
		Object col = colValue;
		if (NumberUtils.isCreatable(colValue)) {
			// 数字に変換出来たらint型
			clazz = int.class;
			col = Integer.valueOf(colValue);
		} else if (colValue.equalsIgnoreCase("TRUE") || colValue.equalsIgnoreCase("FALSE")) {
			// TRUE or FALSEだったらboolean型
			clazz = boolean.class;
			col = Boolean.valueOf(colValue);
		}
		Method method = Pokedex.class.getDeclaredMethod(methodName, clazz);
		method.setAccessible(true);
		method.invoke(pokedex, col); // Pokedexのセッターの呼び出し
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
