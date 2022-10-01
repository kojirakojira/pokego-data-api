package jp.brainjuice.pokego.filter.jwt;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * JWTの情報を保持します。
 *
 * @author saibabanagchampa
 *
 */
@Component
public class SecurityConst {

	/** タイムアウト値 */
    public static final long EXPIRATION_TIME = 7 * 24 * 60 * 60 * 1000; // 1週間

    /** トークン接頭辞 */
    public static final String TOKEN_PREFIX = "Bearer ";

    /** Authorization */
    public static final String AUTHORIZATION_HEADER_NAME = "Authorization";

    /** JWT接頭辞 */
    public static final String JWT_PREFIX = "JWT_";

    /** ログインエンドポイント */
    public static final String LOGIN_URL = "/api/login";

    /** セキュアエンドポイント（JWTトークンが必須なエンドポイント） */
    public static final String SECURE_ENDPOINT = "/api/secure/**";

    public static String CRYPT_KEY;

    @Value("${jwt.uuid}")
    public void setCryptKey(String uuid) {
    	SecurityConst.CRYPT_KEY = uuid;
    }

}