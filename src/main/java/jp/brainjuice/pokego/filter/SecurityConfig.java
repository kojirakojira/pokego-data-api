package jp.brainjuice.pokego.filter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import jp.brainjuice.pokego.filter.jwt.JwtAuthenticationFilter;
import jp.brainjuice.pokego.filter.jwt.SecurityConst;


@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	@Value("${server.client.origin}")
	private String origin;

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http
				.cors().configurationSource(this.corsConfigurationSource())
				.and().authorizeRequests()
				.antMatchers(SecurityConst.SECURE_ENDPOINT).authenticated()
				.anyRequest().permitAll()
				.and().csrf().disable()
				.addFilter(new JwtAuthenticationFilter(authenticationManager()))
				.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

	}

    /**
     * CORS許可パターン
     *
     * @return
     */
    private CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.addAllowedMethod(CorsConfiguration.ALL);
        corsConfiguration.addAllowedHeader(CorsConfiguration.ALL);
        corsConfiguration.addExposedHeader(SecurityConst.AUTHORIZATION_HEADER_NAME);
        for (String o: origin.split("\\|", 0)) {
        	corsConfiguration.addAllowedOrigin("http://" + o);
        	corsConfiguration.addAllowedOrigin("http://www." + o);
        	corsConfiguration.addAllowedOrigin("https://" + o);
        	corsConfiguration.addAllowedOrigin("https://www." + o);
        }
        corsConfiguration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource corsSource = new UrlBasedCorsConfigurationSource();
        corsSource.registerCorsConfiguration("/**", corsConfiguration);

        return corsSource;
    }

}