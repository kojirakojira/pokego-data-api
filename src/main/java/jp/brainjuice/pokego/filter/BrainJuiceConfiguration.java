package jp.brainjuice.pokego.filter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jp.brainjuice.pokego.filter.interceptor.BrainJuiceInterceptor;

/**
 * InterceptorをBean定義
 *
 * @author amuka
 *
 */
@Configuration
public class BrainJuiceConfiguration {

	@Bean
	public BrainJuiceInterceptor brainJuiceHandlerInterceptor() throws Exception {
		return new BrainJuiceInterceptor();
	}

}
