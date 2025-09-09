package jp.brainjuice.pokego.dao.redis.entity;

import java.io.Serializable;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@RedisHash(value = "last_update")
@ToString
public class LastUpdate implements Serializable, Cloneable {

	@Id
	private String key;
	private String ymd;

	/**
	 * (非 Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
        return key.hashCode();
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

		if (!(obj instanceof LastUpdate)) {
			return false;
		}

		LastUpdate other = (LastUpdate) obj;

		return key != null && key.equals(other.getKey());
	}
}
