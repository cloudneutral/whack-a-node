package io.cockroachdb.wan;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.jdbc.JdbcRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@EnableAspectJAutoProxy(proxyTargetClass = true)
@SpringBootApplication(exclude = {
        JdbcRepositoriesAutoConfiguration.class,
        DataSourceAutoConfiguration.class,
})
public class Application implements ApplicationRunner {
    private static final Logger logger = LoggerFactory.getLogger(Application.class);

    private static Map<String, String> parseTuples(Path path) throws IOException {
        Map<String, String> map = new LinkedHashMap<>();
        Pattern tuples = Pattern.compile("(\\S+?)\\s*=\\s*\"([^\\t]+)\"\\s*");

        if (Files.exists(path)) {
            Files.readAllLines(path).forEach(line -> {
                Matcher m = tuples.matcher(line);
                if (m.matches()) {
                    map.put(m.group(1), m.group(2));
                }
            });
        }

        return Collections.unmodifiableMap(map);
    }

    @Value("${application.adminUrl}")
    private String adminUrl;

    @Value("${server.port}")
    private String serverPort;

    @Override
    public void run(ApplicationArguments args) {
        logger.info("CockroachDB Console base URL: %s".formatted(adminUrl));
        logger.info("Service base URL: http://localhost:%s".formatted(serverPort));
    }

    public static void main(String[] args) {
        Path userConfig = Paths.get("settings.cfg");

        if (Files.exists(userConfig)) {
            try {
                Map<String, String> map = parseTuples(userConfig);
                String securityMode = map.getOrDefault("SECURITY_MODE", "");
                if ("cloud".equals(securityMode)) {
                    Map<String, String> props = new LinkedHashMap<>();
                    props.put("spring.profiles.active", securityMode);
                    props.put("spring.datasource.url", "jdbc:postgresql://%s/%s?sslmode=require&sslrootcert=%s"
                            .formatted(
                                    map.getOrDefault("DB_HOST", ""),
                                    map.getOrDefault("DB_NAME", "defaultdb"),
                                    map.getOrDefault("CC_SSL_ROOT_CERT", "")
                            ));
                    props.put("spring.datasource.username", map.getOrDefault("DB_USER", "craig"));
                    props.put("spring.datasource.password", map.getOrDefault("DB_PASSWORD", "cockroach"));
                    props.put("application.adminUrl", map.getOrDefault("ADMIN_URL", ""));

                    props.forEach((k, v) -> {
                        logger.info("Overriding %s=%s".formatted(k, v));
                        System.setProperty(k, v);
                    });
                }

            } catch (IOException e) {
                logger.warn(e.toString());
            }
        }

        new SpringApplicationBuilder(Application.class)
                .logStartupInfo(true)
                .web(WebApplicationType.SERVLET)
                .run(args);
    }
}
