package com.example.TTECHT;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.assertEquals;


@SpringBootTest
class TtechtApplicationTests {

	@Test
	void contextLoads() {
	}

	@Test
	void testHelloWorld() {
		assertEquals("Hello World", "Hello World");
	}

}
