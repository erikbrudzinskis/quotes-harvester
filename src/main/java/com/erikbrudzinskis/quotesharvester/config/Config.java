package com.erikbrudzinskis.quotesharvester.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@ConfigurationProperties
@Data
public class Config {
    private List<Map<String, String>> instruments;
    private int flushPeriodS;
}
