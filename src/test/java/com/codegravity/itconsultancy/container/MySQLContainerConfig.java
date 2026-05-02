package com.codegravity.itconsultancy.container;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
public class MySQLContainerConfig {

    // WHY static: Static means one container for the entire test JVM session.
    // Without static, Spring would spin up a NEW MySQL Docker container
    // for every single test class — extremely slow in CI.
    @Bean
    @ServiceConnection
    static MySQLContainer<?> mySQLContainer() {
        return new MySQLContainer<>(DockerImageName.parse("mysql:8.0"))
                .withDatabaseName("itconsultancy_test")
                .withUsername("test")
                .withPassword("test")
                .withReuse(true); // Reuse across test runs — faster local dev
    }
}