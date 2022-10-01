package jp.brainjuice.pokego.business.dao;

import org.springframework.data.jpa.domain.Specification;

public class GoPokedexSpecification<T> {

	public Specification<T> nameContains(String name) {

		return (root, query, builder) -> {

			return builder.like(root.get("name"), "%" + name + "%");
		};
	}
}
