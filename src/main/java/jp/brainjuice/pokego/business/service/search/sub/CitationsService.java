package jp.brainjuice.pokego.business.service.search.sub;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Service;

import com.ibm.icu.text.MessageFormat;

import jp.brainjuice.pokego.cache.inmemory.topic.data.PageNameEnum;
import jp.brainjuice.pokego.dao.jpa.CitationsRepository;
import jp.brainjuice.pokego.dao.jpa.entity.Citations;
import jp.brainjuice.pokego.web.search.form.res.elem.Citation;

@Service
public class CitationsService {

    private CitationsRepository citationsRepository;

    public CitationsService(CitationsRepository citationsRepository) {
        this.citationsRepository = citationsRepository;
    }

    /**
     * 指定されたページで利用されている参考文献のリストを返す。
     * 
     * @param page         参照を取得したいページ
     * @param embeddedList URLに埋め込む値を並べたリスト。（displayOrderのリスト<埋め込み文字の中括弧の数値<埋め込み文字>>）
     * @return 参考文献のリスト
     */
    public List<Citation> exec(PageNameEnum page, List<List<String>> embeddedList) {
        List<Citations> refList = citationsRepository.findByPageIdOrderByDisplayOrder(page.name());
        if (refList.isEmpty()) {
            return new ArrayList<>();
        }

        AtomicInteger count = new AtomicInteger(0);
        return refList.stream()
                .map(ref -> {
                    int i = count.getAndAdd(1);
                    Citation r = new Citation();
                    if (embeddedList != null && i < embeddedList.size()) {
                        // 埋め込み文字が存在する
                        Object[] params = embeddedList.get(i).toArray();
                        r.setAuthor(MessageFormat.format(ref.getAuthor(), params));
                        r.setTitle(MessageFormat.format(ref.getTitle(), params));
                        r.setUrl(MessageFormat.format(ref.getUrl(), params));
                    } else {
                        // 埋め込み文字が存在しない
                        r.setAuthor(ref.getAuthor());
                        r.setTitle(ref.getTitle());
                        r.setUrl(ref.getUrl());
                    }

                    return r;
                })
                .toList();
    }
}
