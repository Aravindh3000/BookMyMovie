package com.example.BookMyMovie;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;


@SpringBootTest
@ActiveProfiles("test") // this disables WebSocketConfig
class BookMyMovieApplicationTests {

	@Test
	void contextLoads() {
	}

}
