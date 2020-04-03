package com.gabb.sb.spring;


import ch.qos.logback.classic.Level;
import com.gabb.sb.ResourcePool;
import com.gabb.sb.architecture.DatabaseChangingEventBus;
import com.gabb.sb.architecture.Util;
import io.vertx.core.Vertx;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories
@ComponentScan("com.gabb.sb")
public class ServerSpringBootApplication {

    public static final String HOST = "localhost";
    public static final int PORT = 8081;

    public static void main(String[] args) {
        Util.configureLoggersProgrammatically(Level.INFO);
        var ctx = new SpringApplicationBuilder(ServerSpringBootApplication.class)
                .properties("spring.config.name:server")
                .build(args)
                .run();
        ctx.getBean(DatabaseChangingEventBus.class).start();
        var pool = ResourcePool.getInstance();
        var vertx = Vertx.vertx();
        var server = vertx.createHttpServer();
        server.websocketHandler(pool::add).listen(PORT, HOST);
        LoggerFactory.getLogger(ServerSpringBootApplication.class).info("Vertx Listening on {}:{}", HOST, server.actualPort());
    }

}
