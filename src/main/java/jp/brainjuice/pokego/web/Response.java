package jp.brainjuice.pokego.web;

import jp.brainjuice.pokego.web.search.form.res.MsgLevelEnum;
import lombok.Data;

/**
 * APIにて値を返却する場合は、必ずこのクラスを継承させてください。
 *
 * @author saibabanagchampa
 *
 */
@Data
public abstract class Response {

	private boolean success = true;
	private String message = "";
	private MsgLevelEnum msgLevel = MsgLevelEnum.info;

}
