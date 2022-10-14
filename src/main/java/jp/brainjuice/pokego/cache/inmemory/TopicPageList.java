package jp.brainjuice.pokego.cache.inmemory;

import java.util.ArrayList;
import java.util.Collections;

import org.springframework.stereotype.Component;

import jp.brainjuice.pokego.cache.inmemory.data.TopicPage;


@Component
public class TopicPageList extends ArrayList<TopicPage> {

	public TopicPageList() {
		super(Collections.synchronizedList(new ArrayList<TopicPage>()));
	}
}
