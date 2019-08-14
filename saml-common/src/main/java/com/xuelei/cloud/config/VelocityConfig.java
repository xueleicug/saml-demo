package com.xuelei.cloud.config;

import net.shibboleth.utilities.java.support.velocity.VelocityEngine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VelocityConfig {
    
    @Bean
    public org.apache.velocity.app.VelocityEngine velocityEngine() {
    
        org.apache.velocity.app.VelocityEngine velocityEngine = VelocityEngine.newVelocityEngine();
        velocityEngine.setProperty("UTF-8", "UTF-8");
        velocityEngine.setProperty("resource.loader", "classpath");
        velocityEngine.setProperty("classpath.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        velocityEngine.init();
        return velocityEngine;
    }
}
