package com.devpriyanshu.documentqa;

import com.devpriyanshu.documentqa.config.DocumentQaProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(DocumentQaProperties.class)
public class DocumentQaApplication {

    public static void main(String[] args) {
        SpringApplication.run(DocumentQaApplication.class, args);
    }
}
