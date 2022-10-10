package jp.brainjuice.pokego.filter.jwt;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.FilterChain;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JwtAuthenticationFilter  extends BasicAuthenticationFilter {

	public JwtAuthenticationFilter(AuthenticationManager authenticationManager) {
		super(authenticationManager);
		log.info("JwtAuthorizationFilter Initialize...");
	}

	@Override
	protected void doFilterInternal(HttpServletRequest req,
			HttpServletResponse res,
			FilterChain chain) throws IOException, ServletException {
		String header = req.getHeader(SecurityConst.AUTHORIZATION_HEADER_NAME);

		if (header == null || !header.startsWith(SecurityConst.JWT_PREFIX)) {
			chain.doFilter(req, res);
			return;
		}

		try {
			UsernamePasswordAuthenticationToken authentication = getAuthentication(req);

			// jwtの期限を更新
			// これにより、ログイン後アクセスするたびにjwtの期限が更新される
			String token = JwtTokenCreater.createToken(authentication.getPrincipal().toString());
			res.addHeader(SecurityConst.AUTHORIZATION_HEADER_NAME,
					SecurityConst.JWT_PREFIX + token);
			SecurityContextHolder.getContext().setAuthentication(authentication);
			chain.doFilter(req, res);
		} catch (Exception e) {
			// エラー時は/errorFilterにぶん投げてハンドリングする
			log.error(e.getMessage(), e);
			RequestDispatcher rd = req.getRequestDispatcher("/errorFilter?exception=" + e.getClass().getName());
			rd.forward(req, res);
			return;
		}

	}

	private UsernamePasswordAuthenticationToken getAuthentication(HttpServletRequest request) {
		String token = request.getHeader(SecurityConst.AUTHORIZATION_HEADER_NAME);
		if (token != null) {
			String user = Jwts.parser()
					.setSigningKey(SecurityConst.CRYPT_KEY.getBytes())
					.parseClaimsJws(token.replace(SecurityConst.JWT_PREFIX, ""))
					.getBody()
					.getSubject();

			if (user != null) {
				return new UsernamePasswordAuthenticationToken(user, null, new ArrayList<>());
			}
			return null;
		}
		return null;
	}
}