package jp.brainjuice.pokego.filter.jwt;

import javax.crypto.SecretKey;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.ibm.icu.text.MessageFormat;

import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

/**
 * JWTの情報を保持します。
 *
 * @author saibabanagchampa
 *
 */
@Component
@Slf4j
public class SecurityConst {

	/** タイムアウト値 */
	public static final long EXPIRATION_TIME = 7 * 24 * 60 * 60 * 1000; // 1週間

	/** トークン接頭辞 */
	public static final String TOKEN_PREFIX = "Bearer ";

	/** Authorization */
	public static final String AUTHORIZATION_HEADER_NAME = "Authorization";

	/** JWT接頭辞 */
	public static final String JWT_PREFIX = "JWT_";

	/** セキュアエンドポイント（JWTトークンが必須なエンドポイント） */
	public static final String SECURE_ENDPOINT = "/api/secure/**";

	public static SecretKey CRYPTO_KEY;

	@Value("${jwt.cryptoKey}")
	public void setCryptoKey(String cryptoKey) {
		log.info(MessageFormat.format("CRYPTO_KEY: {0}", cryptoKey));
		if (StringUtils.isEmpty(cryptoKey)) {
			return;
		}
		CRYPTO_KEY = Keys.hmacShaKeyFor(cryptoKey.getBytes());
	}

}