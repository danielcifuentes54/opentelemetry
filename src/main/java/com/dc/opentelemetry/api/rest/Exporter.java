package com.dc.opentelemetry.api.rest;

import java.util.ArrayList;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Tag;
import io.micrometer.logzio.LogzioConfig;
import io.micrometer.logzio.LogzioMeterRegistry;
import jakarta.annotation.PostConstruct;

@RestController
@RequestMapping("/api/exporter")
public class Exporter {

    private static final Logger logger = LoggerFactory.getLogger(Exporter.class);
    private static final String PHRASE = "This is an example log message but for the v2 version";

    @Autowired
    private LogzioConfig logzioConfig;

    private Counter counter;

    @PostConstruct
    public void init() {
        LogzioMeterRegistry registry = new LogzioMeterRegistry(logzioConfig, Clock.SYSTEM);

        // Initialize the counter once the bean is created
        ArrayList<Tag> tags = new ArrayList<>();
        tags.add(Tag.of("env", "dev-micrometer"));

        // Create and register the counter once
        this.counter = Counter
                .builder("koronet_counter")
                .description("custom metric to test logz - v2")
                .tags(tags)
                .register(registry);
    }

    @GetMapping(path = "/log")
    public ResponseEntity<String> generaLog() {
        logger.info("Logging phrase: {}", PHRASE);
        return new ResponseEntity<>("Old OK", HttpStatus.OK);
    }

    @GetMapping(path = "/logv2")
    public ResponseEntity<String> generaLog() {
        logger.info("Logging phrase: {}", PHRASE);
        return new ResponseEntity<>("OK - v2", HttpStatus.OK);
    }

    @GetMapping(path = "/log/{value}")
    public ResponseEntity<String> generaLog(@PathVariable("value") Integer value) {
        IntStream.rangeClosed(1, value).forEach(n
                -> logger.info("iteration: {} - Logging phrase: {}", n, PHRASE)
        );
        return new ResponseEntity<>("OK", HttpStatus.OK);
    }

    @GetMapping(path = "/exception")
    public ResponseEntity<String> generateException() {
        try {
            int a = 1 / 0;
        } catch (ArithmeticException e) {
            logger.error("An error occurred: {}", e.getMessage(), e);
            return new ResponseEntity<>("v2 Error", HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>("OK", HttpStatus.OK);
    }

    @GetMapping(path = "/custom-metric")
    public ResponseEntity<String> generateCustomMetric() {
        // Increment your counter
        counter.increment();
        return new ResponseEntity<>("OK", HttpStatus.OK);
    }
}
