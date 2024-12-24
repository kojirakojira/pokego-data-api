package jp.brainjuice.pokego.cache.inmemory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import jp.brainjuice.pokego.business.dao.RaceExceptionsRepository;
import jp.brainjuice.pokego.business.dao.entity.RaceExceptions;
import jp.brainjuice.pokego.cache.inmemory.dto.RaceEx;
import jp.brainjuice.pokego.utils.exception.PokemonDataInitException;
import lombok.extern.slf4j.Slf4j;

/**
 * 種族値例外をローカルに保持するためのクラス
 */
@Component
@Slf4j
public class RaceExceptionsMap extends HashMap<String, Map<RaceEx, Object>> {


	public RaceExceptionsMap(RaceExceptionsRepository raceExceptionsRepository) throws PokemonDataInitException {

		try {
			List<RaceExceptions> reList =  raceExceptionsRepository.findAll();

			HashMap<String, Map<RaceEx, Object>> raceExMap = reList.stream()
					.map(re -> Map.entry(re.getPokedexId(), re))
					.collect(Collectors.toMap(
							Map.Entry::getKey,
							entry -> {
								Map<RaceEx, Object> map = new HashMap<>();
								RaceExceptions re = entry.getValue();
								if (re.getAttack() != null) {
									map.put(RaceEx.ATTACK, re.getAttack());
								}
								if (re.getDefense() != null) {
									map.put(RaceEx.DEFENSE, re.getDefense());
								}
								if (re.getHp() != null) {
									map.put(RaceEx.HP, re.getHp());
								}
								if (re.getNotExistsOrigin() != null) {
									map.put(RaceEx.NOT_EXISTS_ORIGIN, re.getNotExistsOrigin());
								}
								return map;
							},
							(a, b) -> a,
							HashMap::new));
			putAll(raceExMap);

		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new PokemonDataInitException(e);
		}
	}

	/**
	 * 指定したポケモンが原作に存在するかを判定する。<br>
	 * ※存在しないpokedexIdを指定した場合もtrueを返却する。
	 *
	 * @param pokedexId
	 * @return
	 */
	public boolean existsOrigin(String pokedexId) {
		Map<RaceEx, Object> raceExMap = get(pokedexId);
		if (raceExMap == null) {
			return true;
		}

		Boolean notExistsOrigin = (Boolean) raceExMap.get(RaceEx.NOT_EXISTS_ORIGIN);

		return notExistsOrigin == null || !notExistsOrigin.booleanValue();
	}
}
