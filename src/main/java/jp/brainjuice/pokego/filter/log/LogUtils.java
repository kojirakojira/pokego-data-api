package jp.brainjuice.pokego.filter.log;

import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogUtils {
    protected final static Logger logger = LoggerFactory.getLogger(LogUtils.class);

    public static void info(String msg) {
        logger.info(msg);
    }
    public static void warn(String msg) {
        logger.warn(msg);
    }
    public static void error(String msg) {
        logger.error(msg);
    }
    public static void debug(String msg) {
        logger.debug(msg);
    }
    public static void trace(String msg) {
        logger.trace(msg);
    }

    public static void error(String msg, Throwable e) {
        logger.error(msg, e);
    }

    /**
     * 指定されたクラスのログクラスを取得
     *
     * @param clazz
     * @return
     */
    public static Log getLog(Object obj) {
    	return LogFactory.getLog(obj.getClass());
    }

    public static Log getLog(Class<?> clazz) {
    	return LogFactory.getLog(clazz);
    }
}