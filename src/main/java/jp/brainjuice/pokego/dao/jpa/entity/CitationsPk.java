package jp.brainjuice.pokego.dao.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import lombok.EqualsAndHashCode;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class CitationsPk {
    /** 参照元ページID */
    @Id
    @Column(name = "page_id", nullable = false, columnDefinition = "bpchar")
    private String pageId;

    /** 表示順序 */
    @Id
    @Column(name = "display_order", nullable = false)
    private int displayOrder;
}
