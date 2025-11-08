package jp.brainjuice.pokego.business.constant;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Component;

import jp.brainjuice.pokego.business.constant.Type.TypeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 天候ブースト
 */
@Component
public class WeatherBoosts {

	/**
	 * タイプ: 天候ブーストのマップ<br>
	 * 不変なマップ
	 */
	private final Map<TypeEnum, WeatherEnum> typeWbMap = createTypeWbMap();

	/**
	 * 天候ブースト: タイプの逆引き用マップ<br>
	 * 不変なマップ
	 */
	private final Map<WeatherEnum, List<TypeEnum>> typeWbLookupMap = createLookupMap();

	@AllArgsConstructor
	public enum WeatherEnum implements ConstantEnumInterface {
		sunny_clear("晴れ"),
		rainy("雨"),
		cloudy("曇り"),
		partly_cloudy("ときどき曇り"),
		windy("強風"),
		snow("雪"),
		fog("霧");

		@Getter
		private final String jpn;
	}

	private Map<TypeEnum, WeatherEnum> createTypeWbMap() {
		Map<TypeEnum, WeatherEnum> map = Stream.of(TypeEnum.values())
				.map(te -> {
					WeatherEnum wbe = switch (te) {
					case normal -> WeatherEnum.partly_cloudy; // ノーマル
					case fire -> WeatherEnum.sunny_clear; // ほのお
					case water -> WeatherEnum.rainy; // みず
					case grass -> WeatherEnum.sunny_clear; // くさ
					case electric -> WeatherEnum.rainy; // でんき
					case ice -> WeatherEnum.snow; // こおり
					case fighting -> WeatherEnum.cloudy; // かくとう
					case poison -> WeatherEnum.cloudy; // どく
					case ground -> WeatherEnum.sunny_clear; // じめん
					case flying -> WeatherEnum.windy; // ひこう
					case psychic -> WeatherEnum.windy; // エスパー
					case bug -> WeatherEnum.rainy; // むし
					case rock -> WeatherEnum.partly_cloudy; // いわ
					case ghost -> WeatherEnum.fog; // ゴースト
					case dragon -> WeatherEnum.windy; // ドラゴン
					case dark -> WeatherEnum.fog; // あく
					case steel -> WeatherEnum.snow; // はがね
					case fairy -> WeatherEnum.cloudy; // フェアリー
					default -> null;
					};
					return Map.entry(te, wbe);
				})
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
		// 不変なマップ
		return Collections.unmodifiableMap(map);
	}

	private Map<WeatherEnum, List<TypeEnum>> createLookupMap() {
		Map<WeatherEnum, List<TypeEnum>> map = getTypeWbMap().entrySet().stream()
				.collect(Collectors.groupingBy(
						Map.Entry::getValue,
						Collectors.mapping(
								Map.Entry::getKey,
								Collectors.toList())));
		// 不変なマップ
		return Collections.unmodifiableMap(map);
	}

	/**
	 * @return タイプ: 天候ブーストの不変なマップ
	 */
	public Map<TypeEnum, WeatherEnum> getTypeWbMap() {
		return typeWbMap;
	}
	/**
	 * @return 天候ブースト：タイプの逆引きリスト
	 */
	public Map<WeatherEnum, List<TypeEnum>> getTypeWbLookupMap() {
		return typeWbLookupMap;
	}
}
