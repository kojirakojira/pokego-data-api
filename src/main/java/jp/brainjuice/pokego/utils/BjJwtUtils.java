package jp.brainjuice.pokego.utils;

import java.util.List;
import java.util.Map;

import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.HttpServletRequest;
import jp.brainjuice.pokego.filter.jwt.SecurityConst;

public class BjJwtUtils {


	/**
	 * トークンとユーザIDを比較し、ログインしているユーザで実行しているかを確認する（念のため）。<br />
	 * 一致した場合のみtrueを返却する。
	 *
	 * @param req
	 * @param userId
	 * @return Boolean
	 */
	public static boolean checkUser(HttpServletRequest req, String userId) {

		String token = req.getHeader(SecurityConst.AUTHORIZATION_HEADER_NAME);

		if (token == null || !token.startsWith(SecurityConst.JWT_PREFIX) || userId == null) {
			return false;
		}

		String tokenUserId = Jwts.parser()
				.setSigningKey(SecurityConst.CRYPT_KEY.getBytes())
				.parseClaimsJws(token.replace(SecurityConst.JWT_PREFIX, ""))
				.getBody()
				.getSubject();

		return userId.equals(tokenUserId);
	}

	/**
	 * JWTトークンに設定されているユーザIDを取得する。
	 *
	 * @return
	 */
	public static String getTokenUserId(HttpServletRequest req) {

		String token = req.getHeader(SecurityConst.AUTHORIZATION_HEADER_NAME);

		if (token == null || !token.startsWith(SecurityConst.JWT_PREFIX)) {
			return null;
		}

		String tokenUserId;
		try {
			tokenUserId = Jwts.parser()
					.setSigningKey(SecurityConst.CRYPT_KEY.getBytes())
					.parseClaimsJws(token.replace(SecurityConst.JWT_PREFIX, ""))
					.getBody()
					.getSubject();
		} catch (Exception e) {
			tokenUserId = null;
		}

		return tokenUserId;
	}

	/**
	 * JWTトークンに設定されているユーザIDを取得する。
	 *
	 * @return
	 */
	public static String getTokenUserId(Map<String, Object> map) {

		@SuppressWarnings("unchecked")
		String token = map.get("nativeHeaders") == null
				? null
				: ((List<String>) ((Map<String, Object>) map.get("nativeHeaders")).get(SecurityConst.AUTHORIZATION_HEADER_NAME)).get(0);

		if (token == null || !token.startsWith(SecurityConst.JWT_PREFIX)) {
			return null;
		}

		String tokenUserId = Jwts.parser()
				.setSigningKey(SecurityConst.CRYPT_KEY.getBytes())
				.parseClaimsJws(token.replace(SecurityConst.JWT_PREFIX, ""))
				.getBody()
				.getSubject();

		return tokenUserId;
	}
}
