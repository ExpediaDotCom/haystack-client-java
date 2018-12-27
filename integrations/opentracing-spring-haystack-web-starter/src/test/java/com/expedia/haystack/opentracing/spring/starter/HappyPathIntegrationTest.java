package com.expedia.haystack.opentracing.spring.starter;

import com.expedia.www.haystack.client.dispatchers.ChainedDispatcher;
import com.expedia.www.haystack.client.dispatchers.Dispatcher;
import com.expedia.www.haystack.client.dispatchers.InMemoryDispatcher;
import com.expedia.www.haystack.client.dispatchers.LoggerDispatcher;
import com.expedia.www.haystack.client.metrics.MetricsRegistry;
import java.io.IOException;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = SimpleServer.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ContextConfiguration(initializers = HappyPathIntegrationTest.Initializer.class)
public class HappyPathIntegrationTest {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private InMemoryDispatcher inMemoryDispatcher;

    @Test
    public void testHaystackWebStarterWiresServerTracesToConfiguredDispatcher() throws IOException {
        final String response = testRestTemplate.getForObject("/helloWorld", String.class);
        Assertions.assertThat(response).isEqualTo("Hello, World!");
        Assertions.assertThat(inMemoryDispatcher.getReportedSpans().size()).isEqualTo(2);
        Assertions.assertThat(
                inMemoryDispatcher.getReportedSpans()
                        .stream()
                        .anyMatch(span -> span.getTags().get("span.kind").equals("server"))).isEqualTo(true);
        Assertions.assertThat(
                inMemoryDispatcher.getReportedSpans()
                        .stream()
                        .anyMatch(span -> span.getTags().get("span.kind").equals("client"))).isEqualTo(true);
        inMemoryDispatcher.flush();
    }

    @Test
    public void testHaystackStarterWiresSpringRestTemplateToConfiguredDispatcher() throws IOException {
        final String response = testRestTemplate.getForObject("/helloWorld", String.class);
        Assertions.assertThat(response).isEqualTo("Hello, World!");
        Assertions.assertThat(inMemoryDispatcher.getReportedSpans().size()).isEqualTo(2);
        Assertions.assertThat(
                inMemoryDispatcher.getReportedSpans()
                        .stream()
                        .anyMatch(span -> span.getTags().get("span.kind").equals("client"))).isEqualTo(true);
        inMemoryDispatcher.flush();
    }

    static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            TestPropertyValues.of("spring.application.name=SimpleServer")
                    .applyTo(configurableApplicationContext);
        }
    }
    
    @Configuration
    static class ContextConfiguration {
        @Bean
        public Dispatcher dispatcher(MetricsRegistry metricsRegistry, InMemoryDispatcher inMemoryDispatcher) {
            final LoggerDispatcher loggerDispatcher = new LoggerDispatcher.Builder(metricsRegistry)
                    .withLogger("haystack").build();
            return new ChainedDispatcher(loggerDispatcher, inMemoryDispatcher);
        }
    }
}
