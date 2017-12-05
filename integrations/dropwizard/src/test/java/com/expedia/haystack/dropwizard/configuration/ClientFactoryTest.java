package com.expedia.haystack.dropwizard.configuration;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import com.google.common.collect.ImmutableList;

import io.dropwizard.configuration.YamlConfigurationFactory;

public class ClientFactoryTest extends BaseFactoryTest<ClientFactory> {
    private final YamlConfigurationFactory<ClientFactory> factory =
        new YamlConfigurationFactory<>(ClientFactory.class, validator, objectMapper, "dw");

    @Test
    public void isDiscoverable() throws Exception {
        // Make sure the types we specified in META-INF gets picked up
        isDiscoverable(ImmutableList.of(AgentClientFactory.class, LoggerClientFactory.class, NoopClientFactory.class));
    }

    @Test
    public void testBuildAgent() throws Exception {
        AgentClientFactory agent = (AgentClientFactory) testFactory(factory, "yaml/client/agent.yml", AgentClientFactory.class);
        assertThat(agent.getPort()).isBetween(1, 65535);
        assertThat(agent.getFormat()).isNotNull();
    }

    @Test
    public void testBuildLogger() throws Exception {
        testFactory(factory, "yaml/client/logger.yml", LoggerClientFactory.class);
    }

    @Test
    public void testBuildNoop() throws Exception {
        testFactory(factory, "yaml/client/noop.yml", NoopClientFactory.class);
    }
}
