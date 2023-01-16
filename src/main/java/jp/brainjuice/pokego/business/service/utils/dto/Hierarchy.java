package jp.brainjuice.pokego.business.service.utils.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Hierarchy {

	private int x;
	private int y;
	private int dist;
	private String id;
	private String bid;

}
