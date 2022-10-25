package jp.brainjuice.pokego.web.form.res;

import lombok.Data;

/**
 * APIにて値を返却する場合は、必ずこのクラスを継承させてください。
 *
 * @author saibabanagchampa
 *
 */
@Data
public abstract class Response {

	private boolean success;
	private String message;
}
