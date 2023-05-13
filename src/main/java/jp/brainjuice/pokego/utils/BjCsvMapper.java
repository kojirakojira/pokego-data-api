package jp.brainjuice.pokego.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.Resource;

import jp.brainjuice.pokego.utils.exception.CsvMappingException;
import lombok.extern.slf4j.Slf4j;

/**
 * CSVをBeanに変換するためのクラスです。<br>
 * CSVの先頭行は変数名を定義してください。
 *
 * @author saibabanagchampa
 *
 */
@Slf4j
public class BjCsvMapper {

	// プリミティブ型のラッパークラスマップ
	private static Map<Class<?>, Class<?>> wrapperMap = new HashMap<>();
	static {
		wrapperMap.put(byte.class, Byte.class);
		wrapperMap.put(short.class, Short.class);
		wrapperMap.put(int.class, Integer.class);
		wrapperMap.put(long.class, Long.class);
		wrapperMap.put(float.class, Float.class);
		wrapperMap.put(double.class, Double.class);
		wrapperMap.put(char.class, Character.class);
		wrapperMap.put(boolean.class, Boolean.class);
	}

	private static final String MSG_METHOD_INVOKE_ERROR = "カラム名の指定、もしくはデータの型に誤りがあります。行番号:{0}, 列番号:{1}";
	private static final String MSG_INDEX_ERROR = "列の個数が一致しませんでした。行番号:{0}, 列番号:{1}";
	private static final String MSG_CSV_METHOD_ERROR = "CSVで定義したカラムが、指定したクラスに存在しません。メソッド名：{0}";
	private static final String MSG_FAILED_CREATE_INSTANCE_ERROR = "インスタンスの生成に失敗しました。{0}";

	private static final String SEPARATOR = ",";

	public static <E> List<E> mapping(String fileName, Class<E> clazz) throws IOException, CsvMappingException, Exception {

		Resource resource = BjUtils.loadFile(fileName);
		BufferedReader br = new BufferedReader(new InputStreamReader(resource.getInputStream()));

		// 行のリスト
		ArrayList<String> rowList = new ArrayList<String>();
		String text;
		while ((text = br.readLine()) != null) {
			rowList.add(text);
		}
		br.close();

		return mapping(rowList, clazz);

	}

	/**
	 * CSV形式の行のリストをE(第２引数のクラス)のリストに変換します。<br>
	 * List<String> → List<E>
	 * @param <E>
	 *
	 * @param rowList
	 * @param element
	 * @return
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws Exception
	 */
	public static <E> List<E> mapping(List<String> rowList, Class<E> clazz) throws CsvMappingException, Exception {

		// 返却値用のリスト
		List<E> eList = new ArrayList<>();

		// コメント行の除去。
		rowList = rowList.stream().filter(r -> r.trim().charAt(0) != '#').collect(Collectors.toList());

		// メソッド名のリストを作成する。csvファイルの1行目はカラム名（キャメルケース）
		List<List<Object>> setterList = new ArrayList<List<Object>>();
		Method[] methods = clazz.getDeclaredMethods();
		for (String col: rowList.get(0).split(SEPARATOR, -1)) {
			String methodName = "set" + StringUtils.capitalize(col);

			Class<?> dataType = getDataType(methodName, methods);

			setterList.add(Arrays.asList(methodName, dataType));
		}

		int x = 0;
		int y = 0;
		int size;
		for (y = 1, size = rowList.size(); y < size; y++) {

			E elem = null;
			try {
				elem = clazz.getDeclaredConstructor().newInstance();

			} catch (Exception e) {
				log.error(MessageFormat.format(MSG_FAILED_CREATE_INSTANCE_ERROR, clazz.getName()), e);
				throw e;
			}

			try {
				// 各項目を設定する。
				String[] colArr = rowList.get(y).split(SEPARATOR, -1); // 末尾の空文字を除去しない。
				if (colArr.length != setterList.size()) {
					// 1行目と対象の行が一致していない場合
					x = colArr.length;
					throw new ArrayIndexOutOfBoundsException();
				}

				for (x = 0; x < colArr.length; x++) {
					// セッターを実行
					invokeSetter(
							elem,
							colArr[x],
							(String) setterList.get(x).get(0),
							(Class<?>) setterList.get(x).get(1));
				}
			} catch (NoSuchMethodException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				String msg = MessageFormat.format(MSG_METHOD_INVOKE_ERROR, y + 1, x);
				log.error(msg);
				throw new CsvMappingException(msg, e);
			} catch (ArrayIndexOutOfBoundsException e) {
				String msg = MessageFormat.format(MSG_INDEX_ERROR, y + 1, x);
				log.error(msg);
				throw new CsvMappingException(msg, e);
			} catch (Exception e) {
				throw new CsvMappingException(e);
			}

			eList.add(elem);
		}

		return eList;
	}

	/**
	 * 第一引数の型を取得する。（セッターの型）
	 *
	 * @param methodName
	 * @param methods
	 * @return
	 * @throws CsvMappingException
	 */
	private static Class<?> getDataType(String methodName, Method[] methods) throws CsvMappingException {

		Method method = null;
		for (Method beanMethod: methods) {
			if (beanMethod.getName().equals(methodName)) {
				method = beanMethod;
				break;
			}
		}
		if (method == null) {
			// csvファイル1行目に指定した変数のセッターが存在しない場合
			throw new CsvMappingException(MessageFormat.format(MSG_CSV_METHOD_ERROR, methodName));
		}
		// 第一引数の型を取得
		return method.getParameterTypes()[0];
	}

	/**
	 * セッターを実行します。
	 *
	 * @param instance
	 * @param value
	 * @param methodName
	 * @param dataType
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws Exception
	 */
	private static void invokeSetter(Object instance, String value, String methodName, Class<?> dataType)
			throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {

		Object obj = null;
		if (dataType == String.class) {
			obj = value;
		} else {
			// valueOfメソッドでString型のvalueから型変換する。
			obj = wrapperMap.get(dataType).getMethod("valueOf", String.class).invoke(null, value);
		}

		// セッターを使用しインスタンスにセットする。
		Method method = instance.getClass().getDeclaredMethod(methodName, dataType);
		method.setAccessible(true);
		method.invoke(instance, obj); // セッターの呼び出し
	}
}
