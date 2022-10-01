package jp.brainjuice.pokego.filter.jwt;

import java.util.Date;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

public class JwtTokenCreater {
    public static String createToken(String user){
        return Jwts.builder()
                .setSubject(user)
                // 有効期限1週間（ミリ秒で指定）
                .setExpiration(new Date(System.currentTimeMillis() + SecurityConst.EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS512, SecurityConst.CRYPT_KEY.getBytes())
                .compact();
    }
}