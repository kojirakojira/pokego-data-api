package jp.brainjuice.pokego.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jp.brainjuice.pokego.filter.interceptor.BrainJuiceInterceptor;

/**
 * Bean定義
 *
 * @author saibabanagchampa
 *
 */
@Configuration
public class BrainJuiceConfiguration {

	@Bean
	BrainJuiceInterceptor brainJuiceHandlerInterceptor() throws Exception {
		return new BrainJuiceInterceptor();
	}

}
