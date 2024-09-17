package jp.brainjuice.pokego.filter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import jp.brainjuice.pokego.filter.jwt.JwtAuthenticationFilter;
import jp.brainjuice.pokego.filter.jwt.SecurityConst;


@Configuration
@EnableWebSecurity
public class SecurityConfig {

	@Value("${server.client.origin}")
	private String origin;

	@Bean
	AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
	    return authenticationConfiguration.getAuthenticationManager();
	}

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
		.cors(cors -> cors
				.configurationSource(corsConfigurationSource()))
		.authorizeHttpRequests(authz -> authz
				.requestMatchers(SecurityConst.SECURE_ENDPOINT).authenticated()
				.anyRequest().permitAll())
		.csrf(csrf -> csrf
				.disable())
		.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

		AuthenticationManager authManager = authenticationManager(http.getSharedObject(AuthenticationConfiguration.class));
		http
		.addFilter(new JwtAuthenticationFilter(authManager));

		return http.build();

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