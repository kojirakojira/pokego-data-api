package jp.brainjuice.pokego.web.manage.req;

import jakarta.validation.constraints.NotEmpty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequest {

	@NotEmpty
	private String userId;
	private String password;
	private String mfaCode;
	
}
