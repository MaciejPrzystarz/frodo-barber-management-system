package pl.frodo.barber;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = FrodoBarberManagementSystemApplication.class)
@ActiveProfiles("test")
class FrodoBarberManagementSystemApplicationTests {

    @Test
    void contextLoads() {
    }
}