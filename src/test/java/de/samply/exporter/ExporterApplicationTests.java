package de.samply.exporter;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;

@Disabled
@SpringBootTest
@ComponentScan(basePackages = {"de.samply"})
class ExporterApplicationTests {

	@Test
	void contextLoads() {
	}

}
