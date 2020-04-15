package com.gabb.sb.runner;

import ch.qos.logback.classic.Level;
import com.gabb.sb.PropertyKeys;
import com.gabb.sb.Util;
import io.vertx.core.http.CaseInsensitiveHeaders;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;

import static com.gabb.sb.server.ServerSpringBootApplication.HOST;
import static com.gabb.sb.server.ServerSpringBootApplication.PORT;

@SpringBootApplication
@EnableAutoConfiguration(exclude = {
        DataSourceAutoConfiguration.class,
        DataSourceTransactionManagerAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class
})
@ComponentScan("com.gabb.sb.runner")
public class TestRunnerSpringBootApplication {

    public static void main(String[] args) {
        Util.configureLoggersProgrammatically(Level.INFO);
        var ctx = new SpringApplicationBuilder(TestRunnerSpringBootApplication.class)
                .properties("spring.config.name:testrunner")
                .build(args)
                .run();

        ctx.getBean(TestExecutor.class).startWebSocket(PORT, HOST, "/");
    }

}
