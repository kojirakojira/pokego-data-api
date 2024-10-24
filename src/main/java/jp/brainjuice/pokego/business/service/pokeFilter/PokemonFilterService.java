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

@Service
public class PokemonFilterService {

	private GoPokedexRepository goPokedexRepository;

	private GoPokedexSpecifications goPokedexSpecifications;

	public PokemonFilterService(
			GoPokedexRepository goPokedexRepository,
			GoPokedexSpecifications goPokedexSpecifications) {
		this.goPokedexRepository = goPokedexRepository;
		this.goPokedexSpecifications = goPokedexSpecifications;
	}

	public List<GoPokedex> findByAny(Map<FilterEnum, FilterParam> filterMap) {

		if (filterMap == null || filterMap.isEmpty()) {
			return new ArrayList<>();
		}

		Specification<GoPokedex> spec = generateSpecification(filterMap);
		List<GoPokedex> goPokedex = goPokedexRepository.findAll(spec);

		return goPokedex;
	}

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
				if (value instanceof List) {
					@SuppressWarnings("unchecked")
					List<GenNameEnum> genList = (List<GenNameEnum>) value;
					Specification<GoPokedex> inSpec = negate ? gpSpec.genNotIn(genList) : gpSpec.genIn(genList);
					spec = appendAndExpression(spec, inSpec);
				}
				break;
			case region:
				if (value instanceof List) {
					@SuppressWarnings("unchecked")
					List<RegionEnum> genList = (List<RegionEnum>) value;
					Specification<GoPokedex> inSpec = negate ? gpSpec.regionNotIn(genList) : gpSpec.regionIn(genList);
					spec = appendAndExpression(spec, inSpec);
				}
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

	private Specification<GoPokedex> appendAndExpression(Specification<GoPokedex> base, Specification<GoPokedex> append) {
		return base == null ? Specification.where(append) : base.and(append);
	}
}
