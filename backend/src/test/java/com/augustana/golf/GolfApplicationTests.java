package com.augustana.golf;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@ActiveProfiles("test")
@TestPropertySource(properties = "spring.flyway.enabled=false")
@SpringBootTest
class GolfApplicationTests {

	@Test
	void contextLoads() {
	}

}
