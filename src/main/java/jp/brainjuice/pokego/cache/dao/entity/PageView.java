package jp.brainjuice.pokego.cache.dao.entity;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Table(name = "page_view")
public class PageView {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "page_view_seq")
	@SequenceGenerator(name = "page_view_seq", sequenceName = "page_view_seq", initialValue = 1, allocationSize = 1)
	@Column(name = "page_view_id")
	private Integer pageViewId;

	@Column(nullable = false)
	private String page;

	@Column(nullable = false)
	private Date ymd;

	@Column(name = "view_count", nullable = false)
	private int viewCount;

}
