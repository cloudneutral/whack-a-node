package io.cockroachdb.wan;

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
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Value("${application.adminUrl}")
    private String adminUrl;

    @Value("${server.port}")
    private String serverPort;

    @Override
    public void run(ApplicationArguments args) {
        logger.info("DB console: %s".formatted(adminUrl));
        logger.info("Web UI: http://localhost:%s".formatted(serverPort));
    }

    public static void main(String[] args) {
        new SpringApplicationBuilder(Application.class)
                .logStartupInfo(true)
                .web(WebApplicationType.SERVLET)
                .run(args);
    }
}
