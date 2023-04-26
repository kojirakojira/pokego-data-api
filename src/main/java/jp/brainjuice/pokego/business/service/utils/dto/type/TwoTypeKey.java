package jp.brainjuice.pokego.business.service.utils.dto.type;

import jp.brainjuice.pokego.business.constant.Type.TypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * ポケモンのタイプを表現します。<br>
 * タイプ1とタイプ2が逆転しているタイプを比較しても、equalsおよびhashCodeで一致します。<br>
 * 例：「みず、ひこう」と、「ひこう、みず」は一致します。<br>
 * また、「ひこう、ひこう」と「ひこう」も一致します。
 *
 *
 * @author saibabanagchampa
 *
 */
@AllArgsConstructor
@Data
public class TwoTypeKey {

	private TypeEnum type1;
	private TypeEnum type2;

	public String toJpnString() {

		StringBuilder sb = new StringBuilder();

		if (type1 != null) {
			sb.append(type1.getJpn());
		}

		if (type1 != null && type2 != null) {
			sb.append(", ");
		}

		if (type2 != null) {
			sb.append(type2.getJpn());
		}

		return sb.toString();
	}

	/**
	 * (非 Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {

		StringBuilder sb = new StringBuilder();

		if (type1 != null) {
			sb.append(type1.name());
		}

		if (type1 != null && type2 != null) {
			sb.append(", ");
		}

		if (type2 != null) {
			sb.append(type2.name());
		}

		return sb.toString();
	}

	/**
	 * (非 Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		if (type1 == type2) {
			// タイプ1 == タイプ2の場合は1タイプとみなす。
			return type1 != null ? type1.hashCode() : 0;
		}
        int result = type1 != null ? type1.hashCode() : 0;
        result = result + (type2 != null ? type2.hashCode() : 0);
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

    	if (!(obj instanceof TwoTypeKey)) {
    		return false;
    	}

    	// this：A, other：B
    	TwoTypeKey other = (TwoTypeKey) obj;

    	boolean typesEqual = (type1 == null && other.type1 == null) || (type1 != null && type1 == other.type1);
    	typesEqual &= (type2 == null && other.type2 == null) || (type2 != null && type2 == other.type2);

    	if (typesEqual) {
    		// A.t1, B.t1, A.t2, B.t2 -> all null
    		// A.t1, B.t1 -> all null && A.t2 == B.t2
    		// A.t1 == B.t1 && A.t2, B.t2 -> all null
    		// A.t1 == B.t1 && A.t2 == B.t2
    		return true;
    	}

    	typesEqual = (type1 == null && other.type2 == null) || (type1 != null && type1 == other.type2);
    	typesEqual &= (type2 == null && other.type1 == null) || (type2 != null && type2 == other.type1);

    	if (typesEqual) {
        	// A.t1, B.t2, A.t2, B.t1 -> all null
        	// A.t1, B.t2 -> all null && A.t2 == B.t1
        	// A.t1 == B.t2 && A.t2, B.t1 -> null
        	// A.t1 == B.t2 && A.t2 == B.t1
    		return true;
    	}

    	typesEqual = type1 == null && type2 == type1 && type2 == other.type2
    			|| type2 == null && type1 == other.type1 && type1 == other.type2
    			|| other.type1 == null && other.type2 == type1 && other.type2 == type2
    			|| other.type2 == null && other.type1 == type1 && other.type1 == type2;
    	// The pattern for true is described below.
    	// A.t1 == null, A.t2 == B.t1 == B.t2
    	// A.t2 == null, A.t1 == B.t1 == B.t2
    	// B.t1 == null, A.t1 == A.t2 == B.t2
    	// B.t2 == null, A.t1 == A.t2 == B.t1
    	return typesEqual;
    }

}
