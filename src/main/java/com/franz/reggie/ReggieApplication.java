package com.franz.reggie;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Slf4j
@SpringBootApplication
@ServletComponentScan//扫描JavaWeb提供的组件，包括Servlet Filter等
@EnableTransactionManagement
public class ReggieApplication {

    public static void main(String[] args) {
//        提示找不到log变量时，是没有为项目开启anatation processor，在设置中开启，并指定正确的maven repository路径
        log.info("Application started ...");
        SpringApplication.run(ReggieApplication.class, args);
    }

}
