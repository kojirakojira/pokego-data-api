package jp.brainjuice.pokego.web.form.res.elem;

import java.util.List;

import jp.brainjuice.pokego.business.dao.entity.GoPokedex;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 進化に関する情報を扱うクラス
 *
 * @author saibabanagchampa
 *
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EvolutionEdge {

	private String pokedexId;
	private String beforePokedexId;

	private GoPokedex goPokedex;
	private GoPokedex beforeGoPokedex;

	/** 別の条件がある場合の注釈。アメの一覧を表示する際、別の進化条件を一覧で保持する。 */
	private List<String> annos;

	/**
	 * (非 Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {

		StringBuilder sb = new StringBuilder();
		sb.append("EvolutionEdge(pid:");

		if (pokedexId != null) {
			sb.append(pokedexId);
		}

		if (pokedexId != null && beforePokedexId != null) {
			sb.append(", befPid:");
		}

		if (beforePokedexId != null) {
			sb.append(beforePokedexId);
		}

		sb.append(")");

		return sb.toString();
	}

	/**
	 * (非 Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		if (pokedexId == beforePokedexId) {
			// タイプ1 == タイプ2の場合は1タイプとみなす。
			return pokedexId != null ? pokedexId.hashCode() : 0;
		}
        int result = pokedexId != null ? pokedexId.hashCode() : 0;
        result = result + (beforePokedexId != null ? beforePokedexId.hashCode() : 0);
        return result;
	}

    /**
     * (非 Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
    	if (this == obj) {
    		return true;
    	}

    	if (!(obj instanceof EvolutionEdge)) {
    		return false;
    	}

    	EvolutionEdge other = (EvolutionEdge) obj;

        if (pokedexId != null ? !pokedexId.equals(other.pokedexId) : other.pokedexId != null) {
        	return false;
        }
        // pokedexId == other.pokedexIdが一致する場合のみ、if文を抜ける。

        if (beforePokedexId != null ? !beforePokedexId.equals(other.beforePokedexId) : other.beforePokedexId != null) {
        	return false;
        }

        return true;
    }
}
