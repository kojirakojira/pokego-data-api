package jp.brainjuice.pokego.filter;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jp.brainjuice.pokego.filter.firebase.FirebaseAuthenticationFilter;
import jp.brainjuice.pokego.filter.jwt.SecurityConst;
import jp.brainjuice.pokego.filter.log.MdcXRequestIdFilter;

/**
 * ServletFilterを管理するコンフィギュレーションクラス
 *
 * @author amuka
 *
 */
@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean<MdcXRequestIdFilter> mdcXRequestIdFilter() {
    	FilterRegistrationBean<MdcXRequestIdFilter> bean = new FilterRegistrationBean<MdcXRequestIdFilter>();
        bean.setFilter(new MdcXRequestIdFilter());
        bean.setOrder(1);
        return bean;
    }

    @Bean
    public FilterRegistrationBean<FirebaseAuthenticationFilter> firebaseAuthenticationFIlter() {

        FilterRegistrationBean<FirebaseAuthenticationFilter> bean = new FilterRegistrationBean<FirebaseAuthenticationFilter>();
        bean.setFilter(new FirebaseAuthenticationFilter());
        bean.addUrlPatterns(SecurityConst.LOGIN_URL);
        bean.setOrder(2);

        return bean;
    }

}