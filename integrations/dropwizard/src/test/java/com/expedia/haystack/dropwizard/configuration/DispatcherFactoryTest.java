package com.expedia.haystack.dropwizard.configuration;

import org.junit.Test;

import com.google.common.collect.ImmutableList;

import io.dropwizard.configuration.YamlConfigurationFactory;

public class DispatcherFactoryTest extends BaseFactoryTest<DispatcherFactory> {
    private final YamlConfigurationFactory<DispatcherFactory> factory =
        new YamlConfigurationFactory<>(DispatcherFactory.class, validator, objectMapper, "dw");

    @Test
    public void isDiscoverable() throws Exception {
        // Make sure the types we specified in META-INF gets picked up
        isDiscoverable(ImmutableList.of(RemoteDispatcherFactory.class,
                                        LoggerDispatcherFactory.class,
                                        RemoteDispatcherFactory.class));
    }

    @Test
    public void testBuildRemote() throws Exception {
        RemoteDispatcherFactory agent = (RemoteDispatcherFactory) testFactory(factory, "yaml/dispatcher/remote.yml", RemoteDispatcherFactory.class);
    }

    @Test
    public void testBuildLogger() throws Exception {
        testFactory(factory, "yaml/dispatcher/logger.yml", LoggerDispatcherFactory.class);
    }

    @Test
    public void testBuildNoop() throws Exception {
        testFactory(factory, "yaml/dispatcher/noop.yml", NoopDispatcherFactory.class);
    }
}
