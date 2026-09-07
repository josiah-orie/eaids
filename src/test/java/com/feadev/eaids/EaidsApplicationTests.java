package com.feadev.eaids;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Application context smoke test.
 *
 * <p>Runs against a disposable MySQL container (matching the app's MySQL-specific DDL) under the
 * {@code integration} profile. The class is skipped automatically when no Docker environment is
 * available, so it never fails on machines without Docker.</p>
 */
@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
@ActiveProfiles("integration")
@Tag("docker")
class EaidsApplicationTests {

	@Container
	static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
			.withDatabaseName("eaids_test")
			.withUsername("test")
			.withPassword("test");

	@DynamicPropertySource
	static void configureProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", mysql::getJdbcUrl);
		registry.add("spring.datasource.username", mysql::getUsername);
		registry.add("spring.datasource.password", mysql::getPassword);
	}

	@Test
	void contextLoads() {
	}

}
