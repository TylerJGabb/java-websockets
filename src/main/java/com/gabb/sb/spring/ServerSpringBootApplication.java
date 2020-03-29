package com.gabb.sb.spring;


import ch.qos.logback.classic.Level;
import ch.qos.logback.core.joran.spi.NoAutoStartUtil;
import com.gabb.sb.architecture.DatabaseChangingEventBus;
import com.gabb.sb.architecture.ResourcePool;
import com.gabb.sb.architecture.Util;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import javax.annotation.PreDestroy;

import static com.gabb.sb.architecture.Server.HOST;
import static com.gabb.sb.architecture.Server.PORT;

@SpringBootApplication
@EnableJpaRepositories
public class ServerSpringBootApplication {

    public static void main(String[] args) {
        Util.configureLoggersProgrammatically(Level.INFO);
        var ctx = new SpringApplicationBuilder(ServerSpringBootApplication.class)
                .web(WebApplicationType.NONE)
                .properties("spring.config.name:server")
                .build(args)
                .run();

        ResourcePool pool = ResourcePool.getInstance();
        DatabaseChangingEventBus.getInstance().start();
        Vertx vertx = Vertx.vertx();
        HttpServer server = vertx.createHttpServer();
        server.websocketHandler(pool::add).listen(PORT, HOST);
        LoggerFactory.getLogger(ServerSpringBootApplication.class).info("Listening on {}:{}", HOST, server.actualPort());
    }

}
