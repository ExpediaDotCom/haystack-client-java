package com.expedia.haystack.dropwizard.configuration;

import org.junit.Test;

import com.google.common.collect.ImmutableList;

import io.dropwizard.configuration.YamlConfigurationFactory;

public class MetricsFactoryTest extends BaseFactoryTest<MetricsFactory> {
    private final YamlConfigurationFactory<MetricsFactory> factory =
        new YamlConfigurationFactory<>(MetricsFactory.class, validator, objectMapper, "dw");

    @Test
    public void isDiscoverable() throws Exception {
        // Make sure the types we specified in META-INF gets picked up
        isDiscoverable(ImmutableList.of(NoopMetricsFactory.class));
    }

    @Test
    public void testBuildStringFormat() throws Exception {
        testFactory(factory, "yaml/metrics/noop.yml", NoopMetricsFactory.class);
    }
}

