package jp.brainjuice.pokego.filter.jwt;

import java.util.Date;

import org.apache.commons.lang3.StringUtils;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JwtTokenCreater {
    public static String createToken(String user){
    	if (StringUtils.isEmpty(SecurityConst.CRYPT_KEY)) {
    		log.warn("UUID not defined. Add JWT_UUID to your environment variables.");
    	}
        return Jwts.builder()
                .setSubject(user)
                // 有効期限1週間（ミリ秒で指定）
                .setExpiration(new Date(System.currentTimeMillis() + SecurityConst.EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS512, SecurityConst.CRYPT_KEY.getBytes())
                .compact();
    }
}