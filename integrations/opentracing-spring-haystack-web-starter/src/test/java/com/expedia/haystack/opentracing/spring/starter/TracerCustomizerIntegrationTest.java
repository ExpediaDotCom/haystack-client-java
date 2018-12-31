package com.expedia.haystack.opentracing.spring.starter;

import com.expedia.haystack.opentracing.spring.starter.support.TracerCustomizer;
import com.expedia.www.haystack.client.Tracer;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@EnableAutoConfiguration
public class TracerCustomizerIntegrationTest {

    @Autowired
    private InvocationCountingTracerCustomizer tracerCustomizer;

    @Test
    public void traceCustomizerInvokedAtStartup() throws Exception {
        Assertions.assertThat(this.tracerCustomizer.getCount()).isEqualTo(1);
    }

    @TestConfiguration
    static class TestContextConfiguration {
        @Bean
        public InvocationCountingTracerCustomizer tracerCustomizer() {
           return new InvocationCountingTracerCustomizer();
        }
    }

    static class InvocationCountingTracerCustomizer implements TracerCustomizer {
        private int counter = 0;

        @Override
        public void customize(Tracer.Builder builder) {
            this.counter++;
        }

        public int getCount() {
            return this.counter;
        }

        public void reset() {
            this.counter = 0;
        }
    }
}
