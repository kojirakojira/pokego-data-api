package jp.brainjuice.pokego.cache.inmemory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import jp.brainjuice.pokego.cache.inmemory.data.TopicPage;


@Component
public class TopicPageList extends ArrayList<TopicPage> {

	public TopicPageList() {
		super(Collections.synchronizedList(new ArrayList<TopicPage>()));
	}

	/**
	 * 使用しない。
	 *
	 * @deprecated
	 */
	@Override
	public boolean add(TopicPage e) {
		return super.add(e);
	}

	/**
	 * 使用しない。
	 *
	 * @deprecated
	 */
	@Override
	public boolean addAll(Collection<? extends TopicPage> topicPages) {
		return super.addAll(topicPages);
	}

	/**
	 * TopicPageリストをセットする。
	 * 上限は10件とする。既に追加していたTopicPageは削除する。
	 *
	 * @param topicPages
	 * @return
	 */
	public boolean setAll(Collection<TopicPage> topicPages) {

		Collection<? extends TopicPage> list = topicPages;
		if (15 < topicPages.size()) {
			list = topicPages.stream()
					.limit(10L) // Listは15件まで。
					.collect(Collectors.toList());
		}
		clear();
		return super.addAll(list);
	}
}
