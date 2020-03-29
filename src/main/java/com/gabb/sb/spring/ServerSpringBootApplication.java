package com.gabb.sb.spring;


import ch.qos.logback.classic.Level;
import com.gabb.sb.architecture.DatabaseChangingEventBus;
import com.gabb.sb.architecture.ResourcePool;
import com.gabb.sb.architecture.Util;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import org.slf4j.LoggerFactory;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import static com.gabb.sb.architecture.Server.HOST;
import static com.gabb.sb.architecture.Server.PORT;

@SpringBootApplication
@EnableJpaRepositories
@ComponentScan("com.gabb.sb")
public class ServerSpringBootApplication {

    public static void main(String[] args) {
        Util.configureLoggersProgrammatically(Level.INFO);
        var ctx = new SpringApplicationBuilder(ServerSpringBootApplication.class)
                .web(WebApplicationType.NONE)
                .properties("spring.config.name:server")
                .build(args)
                .run();

        var dceb = ctx.getBean(DatabaseChangingEventBus.class);
        DatabaseChangingEventBus.getInstance().start();
        ResourcePool pool = ResourcePool.getInstance();
        Vertx vertx = Vertx.vertx();
        HttpServer server = vertx.createHttpServer();
        server.websocketHandler(pool::add).listen(PORT, HOST);
        LoggerFactory.getLogger(ServerSpringBootApplication.class).info("Listening on {}:{}", HOST, server.actualPort());
    }

}
