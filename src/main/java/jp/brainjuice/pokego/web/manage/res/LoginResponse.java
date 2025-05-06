package jp.brainjuice.pokego.web.manage.res;

import jp.brainjuice.pokego.web.search.form.res.Response;
import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
@EqualsAndHashCode(callSuper=false)
public class LoginResponse extends Response {
	
	public enum LoginPhase {
		NO_AUTHORITY,
		NO_PASSWORD_NO_MFA,
		NO_MFA,
		SUCCESS
	}

	private LoginPhase phase = LoginPhase.NO_AUTHORITY;
	private String qrCodeUrl;
	private String jwt;
}
