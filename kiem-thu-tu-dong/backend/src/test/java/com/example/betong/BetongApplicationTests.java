package com.example.betong;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test") // dùng CSDL H2 trong bộ nhớ (src/test/resources/application-test.properties)
class BetongApplicationTests {

    @Test
    void contextLoads() {
    }

}
