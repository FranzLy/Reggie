package com.franz.reggie;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ReggieApplicationTests {

    @Test
    void contextLoads() {
    }

    @Test
    void testDemo() {
        String fileName = "0a3b3288-3446-4420-bbff-f263d0c02d8e.jpg";
        System.out.println("后缀名： " + fileName.substring(fileName.lastIndexOf(".")));
    }

}
