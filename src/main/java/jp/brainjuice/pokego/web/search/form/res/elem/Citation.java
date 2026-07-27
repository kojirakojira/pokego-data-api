package jp.brainjuice.pokego.web.search.form.res.elem;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Citation {
    private String author;
    private String title;
    private String url;
}
