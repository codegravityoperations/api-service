package com.codegravity.itconsultancy;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class ItConsultancyApplicationTests {

	@Test
	void contextLoads() {
	}

}
