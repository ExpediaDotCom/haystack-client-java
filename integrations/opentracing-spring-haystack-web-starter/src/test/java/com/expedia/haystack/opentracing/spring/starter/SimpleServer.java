package com.expedia.haystack.opentracing.spring.starter;

import com.expedia.www.haystack.client.dispatchers.InMemoryDispatcher;
import com.expedia.www.haystack.client.metrics.MetricsRegistry;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@SpringBootApplication
public class SimpleServer {
    public static void main(String[] args) {
        SpringApplication.run(SimpleServer.class, args);
    }

    @GetMapping("/helloWorld")
    public String hello() {
        return "Hello, World!";
    }

    @Bean
    public InMemoryDispatcher inMemoryDispatcher(MetricsRegistry metricsRegistry) {
        return new InMemoryDispatcher.Builder(metricsRegistry).
                withLimit(10).build();
    }
}
