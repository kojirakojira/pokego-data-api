package jp.brainjuice.pokego.filter.log;
import java.io.IOException;
import java.util.UUID;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.slf4j.MDC;

public class MdcXRequestIdFilter implements Filter {

	/** logback内から取得する際のキー */
    public static final String KEY = "x-request-id";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        LogUtils.debug("MdcXRequestIdFilter Initialized...");
    }

    @Override
    public void doFilter(
    		ServletRequest servletRequest,
    		ServletResponse servletResponse,
    		FilterChain filterChain) throws IOException, ServletException {

    	//UUIDの発行
        UUID uuid = UUID.randomUUID();

        try {
        	// MDCに追加
            MDC.put(KEY, uuid.toString());
            filterChain.doFilter(servletRequest, servletResponse);
        } finally {
            MDC.remove(KEY);
        }
    }

	@Override
	public void destroy() {
		// TODO 自動生成されたメソッド・スタブ

	}
}