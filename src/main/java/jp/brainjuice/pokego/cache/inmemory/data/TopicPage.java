package jp.brainjuice.pokego.cache.inmemory.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@Data
@AllArgsConstructor
public class TopicPage {

	/**
	 * ページ（検索パターン）を列挙型で保持する。
	 *
	 * @author saibabanagchampa
	 *
	 */
	public enum PageName {

        raid("レイドボスのCP検索"),
        scpRankList("PvP順位一覧"),
        scpRankMaxMin ("PvP最高(最低)順位個体値"),;

		@Getter
		private final String text;

		private PageName(final String text) {
			this.text = text;
		}
	}

	private String page;
	private String name;
	private String image;
	private int count;

	public TopicPage(String page, String image, int count) {
		setPage(page);
		setName(TopicPage.PageName.valueOf(page).getText());
		setCount(count);
	}
}
