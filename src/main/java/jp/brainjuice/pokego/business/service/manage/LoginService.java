package jp.brainjuice.pokego.business.service.manage;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import com.warrenstrange.googleauth.GoogleAuthenticatorQRGenerator;

import io.micrometer.common.util.StringUtils;
import jp.brainjuice.pokego.dao.redis.AdminRepository;
import jp.brainjuice.pokego.dao.redis.entity.Admin;
import jp.brainjuice.pokego.filter.jwt.BjJwtUtils;
import jp.brainjuice.pokego.utils.exception.AuthenticationFailedException;
import jp.brainjuice.pokego.web.manage.res.LoginResponse;
import jp.brainjuice.pokego.web.manage.res.LoginResponse.LoginPhase;

@Service
public class LoginService {
	
	private AdminRepository adminRepository;
	
	private PasswordEncoder passwordEncoder;
	
	public LoginService(
			AdminRepository adminRepository, 
			PasswordEncoder passwordEncoder) {
		this.adminRepository = adminRepository;
		this.passwordEncoder = passwordEncoder;
	}

	/**
	 * ログインする
	 *
	 * @param userId
	 * @param password
	 * @param mfaCode
	 * @param req
	 * @return
	 * @throws Exception
	 */
	public LoginResponse login(String userId, String password, String mfaCode, HttpServletRequest req) throws Exception {

		LoginResponse res = new LoginResponse();

		GoogleAuthenticator gAuth = new GoogleAuthenticator();

		// ユーザIDからユーザ情報を取得
		Admin admin = adminRepository.findById(userId).orElseThrow(AuthenticationFailedException::new);
		
		if (StringUtils.isEmpty(admin.getPassword())) {
			// パスワードが存在しない場合 -> ユーザ登録はしているが、まだ一度もログインしていないケース

			GoogleAuthenticatorKey secretKey = gAuth.createCredentials();
			String qrCodeUrl = GoogleAuthenticatorQRGenerator.getOtpAuthURL("Peridex", userId, secretKey);
			
			// パスワードを暗号化
			String encodedPassword = passwordEncoder.encode(password);
			
			// パスワードとシークレットキーを一時保存
			admin.setPassword(encodedPassword);
			admin.setTempSecretKey(secretKey.getKey());
			adminRepository.save(admin);
			
			res.setPhase(LoginPhase.NO_PASSWORD_NO_MFA);
			res.setQrCodeUrl(qrCodeUrl);
			return res;
		}
		
		// パスワードの壁
		if (!passwordEncoder.matches(password, admin.getPassword())) {
			throw new AuthenticationFailedException("パスワードが一致しない");
		}

		// シークレットキーの壁
		if (StringUtils.isEmpty(admin.getSecretKey())) {
			// パスワードは登録されているがシークレットキーが登録されていない場合
			//  -> パスワードは入力したが、MFAコードでの一時シークレットキーの確認がまだ
			if (StringUtils.isEmpty(mfaCode)) {
				// シークレットキーが登録されていないのにMFAコードが送信されていない
				GoogleAuthenticatorKey secretKey = gAuth.createCredentials();
				String qrCodeUrl = GoogleAuthenticatorQRGenerator.getOtpAuthURL("Peridex", userId, secretKey);
				
				res.setPhase(LoginPhase.NO_MFA);
				res.setQrCodeUrl(qrCodeUrl);
				return res;
			} 
			
			// シークレットキーの確認
			String tempSecretKey = admin.getTempSecretKey();
			boolean isCodeValid = gAuth.authorize(tempSecretKey, Integer.parseInt(mfaCode));
			if (isCodeValid) {
				admin.setTempSecretKey(null);
				admin.setSecretKey(tempSecretKey);
				// adminを更新し、シークレットキーを確定
				adminRepository.save(admin);

				String jwt = BjJwtUtils.encodeJwt(admin.getUserId());
				res.setPhase(LoginPhase.SUCCESS);
				res.setJwt(jwt);
				return res;
			} else {
				throw new AuthenticationFailedException("MFAが一致しない");
			}
		}
		
		if (StringUtils.isEmpty(mfaCode)) {
			throw new AuthenticationFailedException("MFAコードが未入力");
		}

		boolean isCodeValid = gAuth.authorize(admin.getSecretKey(), Integer.parseInt(mfaCode));
		if (!isCodeValid) {
			throw new AuthenticationFailedException("MFAコードが正しくない");
		}

		// ログイン成功
		String jwt = BjJwtUtils.encodeJwt(admin.getUserId());
		res.setPhase(LoginPhase.SUCCESS);
		res.setJwt(jwt);
		return res;
	}
}
