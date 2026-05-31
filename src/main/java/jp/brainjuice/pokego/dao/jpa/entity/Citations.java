package jp.brainjuice.pokego.dao.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
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
@Table(name = "citations")
@IdClass(CitationsPk.class)
public class Citations {

    /** 参照元ページID */
    @Id
    @Column(name = "page_id", nullable = false, columnDefinition = "bpchar")
    private String pageId;

    /** 表示順序 */
    @Id
    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    /** 執筆元 */
    @Column(length = 64)
    private String author;

    /** タイトル */
    @Column(length = 128)
    private String title;

    /** URL */
    @Column(nullable = false, length = 256)
    private String url;

}
