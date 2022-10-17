package jp.brainjuice.pokego.cache.dao.entity;

import java.io.Serializable;
import java.util.Date;

public interface TempView extends Serializable {

	public String getId();
	public void setId(String id);
	public String getKey();
	public void setKey(String key);
	public String getIp();
	public void setIp(String ip);
	public Date getViewTime();
	public void setViewTime(Date viewTime);
}
