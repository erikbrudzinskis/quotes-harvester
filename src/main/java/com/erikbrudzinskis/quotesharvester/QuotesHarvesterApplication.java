package com.erikbrudzinskis.quotesharvester;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableBatchProcessing
public class QuotesHarvesterApplication {

    public static void main(String[] args) {
        SpringApplication.run(QuotesHarvesterApplication.class, args);
    }
}
