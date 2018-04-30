package com.expedia.haystack.sleuth.core.configuration;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.cloud.sleuth.autoconfig.TraceAutoConfiguration;

import com.expedia.www.haystack.client.dispatchers.clients.GRPCAgentClient;
import com.expedia.www.haystack.client.dispatchers.clients.InMemoryClient;
import com.expedia.www.haystack.client.dispatchers.clients.NoopClient;

public class HaystackSleuthConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(TraceAutoConfiguration.class, HaystackSleuthAutoConfiguration.class));

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
