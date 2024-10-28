package jp.brainjuice.pokego.config;

import java.net.URI;
import java.net.URISyntaxException;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import com.ibm.icu.text.MessageFormat;
import com.zaxxer.hikari.HikariDataSource;

import lombok.extern.slf4j.Slf4j;

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
	 * （定義の柔軟性が欲しかったから、Herokuの件が良い機会だった。）
	 *
	 * @return
	 * @throws URISyntaxException
	 */
	@Bean
    DataSource dataSource() throws URISyntaxException {

		log.info(MessageFormat.format("databaseUrl: {0}, username: {1}, password: {2}", databaseUrl, username, password));

        URI dbUri = new URI(databaseUrl);

        // 「postgresql://<username>:<password>@<host>:<port>/<dbname>」の形式でも指定可能。
        String user = StringUtils.isEmpty(username) ? dbUri.getUserInfo().split(":")[0] : username;
        String pass = StringUtils.isEmpty(password) ? dbUri.getUserInfo().split(":")[1] : password;
        String jdbcUrl = MessageFormat.format(
        		"jdbc:postgresql://{0}:{1}{2}",
        		dbUri.getHost(),
        		String.valueOf(dbUri.getPort()),
        		dbUri.getPath());

        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(jdbcUrl);
        dataSource.setUsername(user);
        dataSource.setPassword(pass);

        return dataSource;
    }

}
