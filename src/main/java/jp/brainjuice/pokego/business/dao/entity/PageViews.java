package jp.brainjuice.pokego.business.dao.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name="page_views")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageViews implements Serializable {

	/** =SearchPattern */
	@Id
	@Column(name = "page")
	private String page;

	@Column(name = "view_count")
	private Integer viewCount;
}
