package jp.brainjuice.pokego.filter.firebase;

import java.io.FileInputStream;
import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;

import jp.brainjuice.pokego.filter.firebase.exception.FirebaseAuthenticationException;
import lombok.extern.slf4j.Slf4j;


/**
 * firebaseによる認証を行う
 *
 * @author amuka
 *
 */
@Slf4j
public class FirebaseAuthenticationFilter implements Filter {

	public FirebaseAuthenticationFilter() {

		try {
			// firebaseの初期設定
			FileInputStream serviceAccount =
					new FileInputStream("./brain-juice-5ac93-firebase-adminsdk-jrctm-89d698e161.json");

			FirebaseOptions options = FirebaseOptions.builder()
					.setCredentials(GoogleCredentials.fromStream(serviceAccount))
					.setDatabaseUrl("https://brain-juice-5ac93.firebaseio.com")
					.build();

			FirebaseApp.initializeApp(options);
		} catch (IOException e) {
			log.error("Failed to initialize firebase settings.", e);
		}

	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
			throws IOException, ServletException {

		try {
			// firebase認証を行う
			String idToken = ((HttpServletRequest) req).getHeader("firebasetoken");
			if (idToken == null) {
				// IDがnull
				return;
			}
			FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);
			if (decodedToken.getUid() == null) {
				// 認証失敗
				throw new FirebaseAuthenticationException();
			}

		} catch (Exception e) {
			// エラー時は/errorFilterにぶん投げてハンドリングする
			log.error(e.getMessage(), e);
			RequestDispatcher rd = req.getRequestDispatcher("/errorFilter?exception=" + e.getClass().getName());
			try {
				rd.forward(req, res);
			} catch (ServletException | IOException e1) {
				e1.initCause(e);
				e = e1;
			}
			throw new RuntimeException(e);
		}

		chain.doFilter(req, res);
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		// TODO 自動生成されたメソッド・スタブ

	}

	@Override
	public void destroy() {
		// TODO 自動生成されたメソッド・スタブ

	}
}