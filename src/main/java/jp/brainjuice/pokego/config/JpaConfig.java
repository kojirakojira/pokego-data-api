package jp.brainjuice.pokego.config;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import com.zaxxer.hikari.HikariDataSource;

import lombok.extern.slf4j.Slf4j;

/**
 * Postgres接続の設定
 */
@Configuration
@EnableJpaRepositories(basePackages = { "jp.brainjuice.pokego.business.dao", "jp.brainjuice.pokego.cache.dao.jpa" })
@Slf4j
public class JpaConfig {

	@Value("${spring.datasource.url}")
	private String databaseUrl;

	@Value("${spring.datasource.username}")
	private String username;

	@Value("${spring.datasource.password}")
	private String password;

	/**
	 * Heroku PostgreSQLのために追加。HerokuのDATABASE_URLには、prefix:"jdbc:"が付かない。<br>
	 * （この実装がなくても何故か：Heroku上では動くっぽいが、一応手でセットする。）
	 *
	 * @return
	 * @throws URISyntaxException
	 */
	@Bean
    DataSource dataSource() throws URISyntaxException {

		log.info(MessageFormat.format("databaseUrl: {0}, username: {1}, password: {2}", databaseUrl, username, password));

		// databaseUrlにjdbc:がある場合は一旦排除する。
		URI dbUri = new URI(
				databaseUrl != null && databaseUrl.startsWith("jdbc:")
				? databaseUrl.replaceFirst("jdbc:", "") : databaseUrl);

        Map<String, String> queryMap = getQuery(dbUri);

        // 「postgresql://@<host>:<port>/<dbname>?user=<username>&password=<password>」または
        // 「postgresql://<username>:<password>@<host>:<port>/<dbname>」の形式でも指定可能。
        String user = !StringUtils.isEmpty(username) ? username
        		: (queryMap.get("user") != null ? queryMap.get("user") : dbUri.getUserInfo().split(":")[0]);
        String pass = !StringUtils.isEmpty(password) ? username
        		: (queryMap.get("password") != null ? queryMap.get("password") : dbUri.getUserInfo().split(":")[1]);

        String jdbcUrl = MessageFormat.format(
        		"jdbc:postgresql://{0}:{1}{2}",
        		dbUri.getHost(),
        		String.valueOf(dbUri.getPort()),
        		dbUri.getPath());

        if (!StringUtils.isEmpty(dbUri.getQuery())) {
        	jdbcUrl.concat(dbUri.getQuery());
        }

        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(jdbcUrl);
        dataSource.setUsername(user);
        dataSource.setPassword(pass);

        return dataSource;
    }

	/**
	 * queryをMapに変換する。
	 *
	 * @param uri
	 * @return
	 */
	private Map<String, String> getQuery(URI uri) {

		String query = uri.getQuery();

		if (query == null) {
			return Map.of();
		}

		return Arrays.stream(query.split("&"))
			.map(kv -> {
				String[] kvArr = kv.split("=");
				return Map.entry(kvArr[0], kvArr[1]);
			})
			.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	}

}
