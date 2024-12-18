package com.dc.opentelemetry.config;

import java.util.Hashtable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.micrometer.logzio.LogzioConfig;
import jakarta.annotation.PostConstruct;

@Configuration
public class LogzioConfigFactory {

    private static final Logger logger = LoggerFactory.getLogger(LogzioConfigFactory.class);

    @Value("${logzio.listener.host}")
    private String listenerHost;

    @Value("${logzio.metrics.token}")
    private String metricsToken;

    @PostConstruct
    public void init() {
      logger.info("====> Configuration class initialized listenerHost: {} ", listenerHost);
    }

    @Bean
    public LogzioConfig logzioConfig() {
        return new LogzioConfig() {
            @Override
            public String get(String key) {
                return null;
            }

            @Override
            public String uri() {
                return listenerHost;
            }

            @Override
            public String token() {
                return metricsToken;
            }

            @Override
            public Hashtable<String, String> includeLabels() {
                return new Hashtable<>();
            }

            @Override
            public Hashtable<String, String> excludeLabels() {
                return new Hashtable<>();
            }
        };
    }
}
