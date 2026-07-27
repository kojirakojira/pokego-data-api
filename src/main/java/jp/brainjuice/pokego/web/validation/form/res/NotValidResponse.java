package jp.brainjuice.pokego.web.validation.form.res;

import java.util.Map;
import java.util.Set;

import jp.brainjuice.pokego.web.Response;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class NotValidResponse extends Response {

	private boolean validationError = true;
	private Map<String, String> errors;
	private Set<String> messages;
}
