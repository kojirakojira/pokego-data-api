package jp.brainjuice.pokego.business.dao.entity;

import java.io.Serializable;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class Entity implements Cloneable, Serializable {

	public Entity clone() {
		Entity entity = null;
		try {
			entity = (Entity) super.clone();
		} catch (CloneNotSupportedException e) {
			log.error("Clone failed.", e);
		}
		return entity;
	}
}
