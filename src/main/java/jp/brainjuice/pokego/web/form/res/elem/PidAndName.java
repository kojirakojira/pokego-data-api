package jp.brainjuice.pokego.web.form.res.elem;

import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 種族値比較機能において、pid(pokedexId)とnameの1まとまりを表現する。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class PidAndName {

	private String pid;
	private String name;
	
	/**
	 * Springがparseしてくれないから手動でJSONをparseする。
	 * 
	 * @param json
	 * @throws JsonProcessingException 
	 * @throws JsonMappingException 
	 */
	public PidAndName(String json) throws JsonMappingException, JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		@SuppressWarnings("unchecked")
		Map<String, String> map = (Map<String, String>) mapper.readValue(json, Map.class);
		setPid(map.get("pid"));
		setName(map.get("name"));
	}
}
