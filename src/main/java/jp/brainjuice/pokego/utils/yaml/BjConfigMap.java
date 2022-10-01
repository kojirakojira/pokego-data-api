//package jp.brainjuice.pokego.utils.yaml;
//
//import java.io.InputStreamReader;
//import java.util.LinkedHashMap;
//
//import javax.annotation.PostConstruct;
//
//import org.springframework.core.io.DefaultResourceLoader;
//import org.springframework.core.io.Resource;
//import org.springframework.stereotype.Component;
//import org.yaml.snakeyaml.Yaml;
//
//import jp.brainjuice.pokego.filter.log.LogUtils;
//
//
//@Component
//public class BjConfigMap extends LinkedHashMap<String, Object> {
//
//	@PostConstruct
//	public void init() {
//
//		DefaultResourceLoader resourceLoader;
//		InputStreamReader reader;
//		try {
//			resourceLoader = new DefaultResourceLoader();
//			Resource resource = resourceLoader.getResource("classpath:config/bj-config.yml");
//			reader = new InputStreamReader(resource.getInputStream());
//
//			Yaml yaml = new Yaml();
//			this.putAll(yaml.loadAs(reader, BjConfigMap.class));
//
//		} catch (Exception e) {
//			LogUtils.getLog(this).error(e.getMessage(), e);
//		}
//	}
//}
