package com.expedia.haystack.sleuth.core.configuration;

import static io.micrometer.core.instrument.Metrics.globalRegistry;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.After;
import org.junit.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.cloud.sleuth.autoconfig.TraceAutoConfiguration;

import com.expedia.www.haystack.client.dispatchers.clients.GRPCAgentClient;
import com.expedia.www.haystack.client.dispatchers.clients.InMemoryClient;
import com.expedia.www.haystack.client.dispatchers.clients.NoopClient;

import io.micrometer.core.instrument.Meter;

public class HaystackSleuthConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(TraceAutoConfiguration.class, HaystackSleuthAutoConfiguration.class));

    @After
    public void cleanup() {
        globalRegistry.getRegistries().forEach(meterRegistry -> {
            meterRegistry.close();
            meterRegistry.getMeters().forEach(Meter::close);
            globalRegistry.remove(meterRegistry);
        });
    }

    @Test
    public void testInitializeDefaultClientBean() {
        contextRunner.run(
            context -> {
                assertThat(context).hasBean("client");
                assertThat(context).hasSingleBean(NoopClient.class);
            }
        );
    }

    @Test
    public void testInitializeInMemoryClientBean() {
        contextRunner
            .withPropertyValues(
                "spring.sleuth.haystack.client.span.dispatch=inmemory"
            )
            .run(context -> {
                     assertThat(context).hasBean("client");
                     assertThat(context).hasSingleBean(InMemoryClient.class);
                 }
            );
    }

    @Test
    public void testInitializeGrpcAgentClientBean() {
        contextRunner
            .withPropertyValues(
                "spring.sleuth.haystack.client.span.dispatch=grpc"
            )
            .run(context -> {
                     assertThat(context).hasBean("client");
                     assertThat(context).hasSingleBean(GRPCAgentClient.class);
                 }
            );
    }
}
