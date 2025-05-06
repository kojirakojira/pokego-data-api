package jp.brainjuice.pokego.filter.jwt;

import java.io.IOException;
import java.util.ArrayList;

import jakarta.servlet.FilterChain;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import io.jsonwebtoken.ExpiredJwtException;
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
			UsernamePasswordAuthenticationToken authentication = getAuthenticationToken(req);

			// jwtの期限を更新
			// これにより、ログイン後アクセスするたびにjwtの期限が更新される
//			String token = BjJwtUtils.encodeJwt(authentication.getPrincipal().toString());
//			res.addHeader(SecurityConst.AUTHORIZATION_HEADER_NAME, token);
			SecurityContextHolder.getContext().setAuthentication(authentication);
			chain.doFilter(req, res);
		} catch(ExpiredJwtException e) {
			RequestDispatcher rd = req.getRequestDispatcher("/jwtExpiredErrorFilter");
			rd.forward(req, res);
			return;
		} catch (Exception e) {
			// エラー時は/errorFilterにぶん投げてハンドリングする
			log.error(e.getMessage(), e);
			RequestDispatcher rd = req.getRequestDispatcher("/errorFilter?exception=" + e.getClass().getName());
			rd.forward(req, res);
			return;
		}

	}

	private UsernamePasswordAuthenticationToken getAuthenticationToken(HttpServletRequest request) {
		String token = request.getHeader(SecurityConst.AUTHORIZATION_HEADER_NAME);
		if (token == null) {
			return null;
		}
		String userId = BjJwtUtils.decodeJwt(token);

		if (userId == null) {
			return null;
		}
		return new UsernamePasswordAuthenticationToken(userId, null, new ArrayList<>());
	}
}