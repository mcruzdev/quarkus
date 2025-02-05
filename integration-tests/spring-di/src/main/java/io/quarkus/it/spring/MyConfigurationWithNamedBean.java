package io.quarkus.it.spring;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MyConfigurationWithNamedBean {

    @Bean(name = "mySpecialName")
    public String foo() {
        return "foo";
    }
}
