package jp.brainjuice.pokego.cache.inmemory;

import java.util.ArrayList;

import org.springframework.stereotype.Component;

import jp.brainjuice.pokego.cache.inmemory.data.TopicPokemon;
import lombok.extern.slf4j.Slf4j;


@Component
@Slf4j
public class TopicPageList extends ArrayList<TopicPokemon> {

}
