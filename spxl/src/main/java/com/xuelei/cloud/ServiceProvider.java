package com.xuelei.cloud;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

@SpringBootApplication
@ServletComponentScan
@Slf4j
public class ServiceProvider {
    
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(ServiceProvider.class);
        app.setBannerMode(Banner.Mode.CONSOLE);
        app.run(args);
        log.info("#########################################");
        log.info("####### Service Provider Started ########");
        log.info("#########################################");
    }

}
