package jp.brainjuice.pokego.business.service.pokeFilter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import jp.brainjuice.pokego.business.constant.GenNameEnum;
import jp.brainjuice.pokego.business.constant.RegionEnum;
import jp.brainjuice.pokego.business.constant.Type.TypeEnum;
import jp.brainjuice.pokego.business.dao.GoPokedexRepository;
import jp.brainjuice.pokego.business.dao.GoPokedexSpecifications;
import jp.brainjuice.pokego.business.dao.dto.FilterParam;
import jp.brainjuice.pokego.business.dao.entity.GoPokedex;

/**
 * GoPokedexを絞り込むために使用するサービス
 */
@Service
public class GoPokedexFilterService {

	private GoPokedexRepository goPokedexRepository;

	private GoPokedexSpecifications goPokedexSpecifications;

	public GoPokedexFilterService(
			GoPokedexRepository goPokedexRepository,
			GoPokedexSpecifications goPokedexSpecifications) {
		this.goPokedexRepository = goPokedexRepository;
		this.goPokedexSpecifications = goPokedexSpecifications;
	}

	/**
	 * 指定した条件で絞り込み、GoPokedexを検索する。
	 *
	 * @param filterMap
	 * @return
	 */
	public List<GoPokedex> findByAny(Map<FilterEnum, FilterParam> filterMap) {

		if (filterMap == null || filterMap.isEmpty()) {
			return new ArrayList<>();
		}

		Specification<GoPokedex> spec = generateSpecification(filterMap);
		List<GoPokedex> goPokedex = goPokedexRepository.findAll(spec);

		return goPokedex;
	}

	/**
	 * 指定した条件で絞り込み、pokedexIdを検索する。
	 *
	 * @param filterMap
	 * @return
	 */
	public List<String> findIdByAny(Map<FilterEnum, FilterParam> filterMap) {

		if (filterMap == null || filterMap.isEmpty()) {
			return new ArrayList<>();
		}

		Specification<GoPokedex> spec = generateSpecification(filterMap);
		List<GoPokedex> goPokedex = goPokedexRepository.findAll(spec);

		return goPokedex.stream()
				.map(GoPokedex::getPokedexId)
				.toList();
	}

	/**
	 * Specificationを生成する。
	 *
	 * @param filterMap
	 * @return
	 */
	public Specification<GoPokedex> generateSpecification(Map<FilterEnum, FilterParam> filterMap) {

		if (filterMap == null || filterMap.isEmpty()) {
			return null;
		}

		GoPokedexSpecifications gpSpec = goPokedexSpecifications;
		Specification<GoPokedex> spec = null;
		for (Map.Entry<FilterEnum, FilterParam> fp: filterMap.entrySet()) {
			Object value = fp.getValue().getFilterValue();
			boolean negate = fp.getValue().isNegate();
			switch (fp.getKey()) {
			case finEvo:
				spec = appendAndExpression(spec, gpSpec.finEvoEqual(!negate));
				break;
			case impled:
				spec = appendAndExpression(spec, gpSpec.implFlgEqual(!negate));
				break;
			case mega:
				spec = appendAndExpression(spec, gpSpec.megaEqual(!negate));
				break;
			case tooStrong:
				spec = appendAndExpression(spec, gpSpec.tooStrongEqual(!negate));
				break;
			case gen:
				@SuppressWarnings("unchecked")
				List<GenNameEnum> genList = (List<GenNameEnum>) value;
				Specification<GoPokedex> genInSpec = negate ? gpSpec.genNotIn(genList) : gpSpec.genIn(genList);
				spec = appendAndExpression(spec, genInSpec);
				break;
			case region:
				@SuppressWarnings("unchecked")
				List<RegionEnum> regionList = (List<RegionEnum>) value;
				Specification<GoPokedex> regionInSpec = negate ? gpSpec.regionNotIn(regionList) : gpSpec.regionIn(regionList);
				spec = appendAndExpression(spec, regionInSpec);
				break;
			case twoType:
				@SuppressWarnings("unchecked")
				List<TypeEnum> typeList = (List<TypeEnum>) value;
				spec = appendAndExpression(spec, gpSpec.twoTypeEqual(typeList.get(0), typeList.get(1)));
				break;
			case type:
				spec = appendAndExpression(spec, gpSpec.typeEqual((TypeEnum) value));
				break;
			default:
				break;
			}
		}
		return spec;
	}

	/**
	 * WHERE句、AND句を状態に応じて連結させる。
	 *
	 * @param base
	 * @param append
	 * @return
	 */
	private Specification<GoPokedex> appendAndExpression(Specification<GoPokedex> base, Specification<GoPokedex> append) {
		return base == null ? Specification.where(append) : base.and(append);
	}
}
