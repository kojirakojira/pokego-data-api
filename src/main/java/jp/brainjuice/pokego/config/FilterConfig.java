package jp.brainjuice.pokego.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jp.brainjuice.pokego.filter.log.MdcXRequestIdFilter;
import jp.brainjuice.pokego.filter.log.RequestLoggingFilter;

/**
 * ServletFilterを管理するコンフィギュレーションクラス
 *
 * @author amuka
 *
 */
@Configuration
public class FilterConfig {

    @Bean
    FilterRegistrationBean<MdcXRequestIdFilter> mdcXRequestIdFilter() {
    	FilterRegistrationBean<MdcXRequestIdFilter> bean = new FilterRegistrationBean<MdcXRequestIdFilter>();
        bean.setFilter(new MdcXRequestIdFilter());
        bean.setOrder(1);
        return bean;
    }

    @Bean
    RequestLoggingFilter requestLoggingFilter() {
      RequestLoggingFilter filter = new RequestLoggingFilter();
      filter.setAfterMessagePrefix("[");
      filter.setIncludeClientInfo(true);
      filter.setIncludeQueryString(true);
//      filter.setIncludeHeaders(true);
      filter.setIncludePayload(true);
      filter.setMaxPayloadLength(1024);
      return filter;
    }

}