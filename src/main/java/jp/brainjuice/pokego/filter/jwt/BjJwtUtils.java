package jp.brainjuice.pokego.filter.jwt;

import java.util.Date;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Jwts;
import jp.brainjuice.pokego.utils.BjUtils;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class BjJwtUtils {

	/**
	 * Jwtを生成する。
	 * 
	 * @param userId
	 * @return
	 */
	public static String encodeJwt(String userId){
		if (SecurityConst.CRYPTO_KEY == null) {
			log.warn("jwt.cryptoKey not defined. Add CRYPTO_KEY to your environment variables.");
		}
		
		Date now = BjUtils.now();

		Date expiration = new Date(now.getTime() + SecurityConst.EXPIRATION_TIME);

		String jwt = Jwts.builder()
				.subject(userId)
				.issuedAt(now)
				.expiration(expiration)
				.signWith(SecurityConst.CRYPTO_KEY)
				.compact();
		
		return SecurityConst.JWT_PREFIX + jwt;
    }

	/**
	 * トークンとユーザIDを比較し、ログインしているユーザで実行しているかを確認する。
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

		String decodedUserId = decodeJwt(token);

		return userId.equals(decodedUserId);
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

		String decodedUserId;
		try {
			decodedUserId = decodeJwt(token);
		} catch (Exception e) {
			decodedUserId = null;
		}

		return decodedUserId;
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

		String decodedUserId = decodeJwt(token);

		return decodedUserId;
	}

	/**
	 * jwtからuserIdを取得する。
	 * 
	 * @param jwt
	 * @return userId
	 */
	public static String decodeJwt(String token) {

		String jwt = token.replace(SecurityConst.JWT_PREFIX, "");
		String decodedUserId = Jwts.parser()
				.verifyWith(SecurityConst.CRYPTO_KEY)
				.build()
				.parseSignedClaims(jwt)
				.getPayload()
				.getSubject();
		
		return decodedUserId;
	}
}
